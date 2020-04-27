package com.example.myapplication.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;
import android.util.LongSparseArray;

import androidx.annotation.NonNull;

import com.coremedia.iso.boxes.Container;
import com.example.myapplication.Events.GenerateVideoBitmapListEvent;
import com.googlecode.mp4parser.FileDataSourceViaHeapImpl;
import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder;
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator;
import com.googlecode.mp4parser.authoring.tracks.AppendTrack;
import com.googlecode.mp4parser.authoring.tracks.CroppedTrack;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class MediaUtil {

    /**
     * return time in milli seconds..
     * @param context
     * @param uri       URI of the video
     * @return
     */
    public static long  getVideoDuration(@NonNull Context context, Uri uri) {
        try {
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(context, uri);
            String time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            long timeInMillisec = Long.parseLong(time );
            retriever.release();

            return timeInMillisec;
        }catch (Exception e) { }

        return 0;
    }

    public static void generateThumbnailBitmapList(long videoDurationMs, Uri uri, int containerWidth, int bitmapDimen, Context context){
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    LongSparseArray<Bitmap> bitmapList = new LongSparseArray<>();

                    //get video duration and parse into micro sec
                    long videoDurationMicos = videoDurationMs * 1000;

                    //calculate the # of bitmaps needed
                    int bitmapCount = (int) Math.ceil(((float) containerWidth) / bitmapDimen);

                    MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
                    mediaMetadataRetriever.setDataSource(context,uri);

                    //calculate where to cut
                    final long interval = videoDurationMicos / bitmapCount;

                    for (int i = 0; i < bitmapCount; i++) {
                        Bitmap bitmap = mediaMetadataRetriever.getFrameAtTime(i * interval, MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
                        try {
                            bitmap = Bitmap.createScaledBitmap(bitmap, bitmapDimen, bitmapDimen, false);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        bitmapList.put(i, bitmap);
                    }
                    mediaMetadataRetriever.release();

                    GenerateVideoBitmapListEvent event = new GenerateVideoBitmapListEvent(uri, bitmapList);
                    EventBus.getDefault().post(event);

                } catch (final Throwable e){
                    Thread.getDefaultUncaughtExceptionHandler().uncaughtException(Thread.currentThread(), e);
                }
            }
        });
    }

    public static String convertMillieToHMmSs(long millie) {
        long seconds = (millie / 1000);
        long second = seconds % 60;
        long minute = (seconds / 60) % 60;
        long hour = (seconds / (60 * 60)) % 24;

        if (hour > 0) {
            return String.format("%02d:%02d:%02d", hour, minute, second);
        } else {
            return String.format("%02d:%02d", minute, second);
        }
    }

    public static String getDurationText(long durationInMs) {
        int minutes = (int) (durationInMs / 1000f / 60f);
        int seconds = (int) (durationInMs / 1000f) % 60;
        return formatDuration(minutes) + ":" + formatDuration(seconds);
    }

    private static String formatDuration(int number) {
        return number > 9 ? Integer.toString(number) : "0" + number;
    }

    private static String generateTrimVideoName(String fileName){
        String name = fileName;
        String type = "";
        if(fileName.lastIndexOf(".") > 0){
            name = fileName.substring(0, fileName.lastIndexOf("."));
            type = fileName.substring(fileName.lastIndexOf("."));
        }
        return name + "_trimmed" + type;
    }

    /**
     * Trims a video file.
     * @param uri: uri of the video file
     * @param destDir: File -- out put directory of the video
     * @param startMs: long -- start time in millisec
     * @param endMs: long -- end time in millisec
     * @return Success: non null, Failed: null and exception
     * @throws IOException
     */
    public static String trimVideo(@NonNull Uri uri, @NonNull File destDir, long startMs, long endMs, Context context) throws IOException{
        String fileName = FileUtil.getFileName(uri, context);
        if(TextUtils.isEmpty(fileName)) return null;
        File trimmedFile = new File(destDir, generateTrimVideoName(fileName));

        com.googlecode.mp4parser.authoring.Movie movie = MovieCreator.build(new FileDataSourceViaHeapImpl(FileUtil.getRealPath(context, uri)));

        List<Track> tracks = movie.getTracks();
        movie.setTracks(new LinkedList<>());

        double startTime1 = (double) startMs / 1000;

        double endTime1 = (double) endMs / 1000;

        boolean timeCorrected = false;

        for (Track track : tracks) {
            if (track.getSyncSamples() != null && track.getSyncSamples().length > 0) {
                if (timeCorrected) {
                    // This exception here could be a false positive in case we have multiple tracks
                    // with sync samples at exactly the same positions. E.g. a single movie containing
                    // multiple qualities of the same video (Microsoft Smooth Streaming file)

                    throw new RuntimeException("The startTime has already been corrected by another track with SyncSample. Not Supported.");
                }

                // have to sync to key frame or will get bad frames when crop
                startTime1 = correctTimeToSyncSample(track, startTime1, false);
                endTime1 = correctTimeToSyncSample(track, endTime1, true);
                timeCorrected = true;
            }
        }

        for (Track track : tracks) {
            long currentSample = 0;
            double currentTime = 0;
            double lastTime = -1;
            long startSample1 = -1;
            long endSample1 = -1;

            for (int i = 0; i < track.getSampleDurations().length; i++) {
                long delta = track.getSampleDurations()[i];


                if (currentTime > lastTime && currentTime <= startTime1) {
                    // current sample is still before the new starttime
                    startSample1 = currentSample;
                }
                if (currentTime > lastTime && currentTime <= endTime1) {
                    // current sample is after the new start time and still before the new endtime
                    endSample1 = currentSample;
                }
                lastTime = currentTime;
                currentTime += (double) delta / (double) track.getTrackMetaData().getTimescale();
                currentSample++;
            }
            movie.addTrack(new AppendTrack(new CroppedTrack(track, startSample1, endSample1)));
        }

        if(!trimmedFile.getParentFile().exists()){
            trimmedFile.getParentFile().mkdirs();
        }

        if (!trimmedFile.exists()) {
            trimmedFile.createNewFile();
        }

        Container out = new DefaultMp4Builder().build(movie);

        FileOutputStream fos = new FileOutputStream(trimmedFile);
        FileChannel fc = fos.getChannel();
        out.writeContainer(fc);

        fc.close();
        fos.close();
        return trimmedFile.getPath();
    }

    private static double correctTimeToSyncSample(@NonNull Track track, double cutHere, boolean next) {
        double[] timeOfSyncSamples = new double[track.getSyncSamples().length];
        long currentSample = 0;
        double currentTime = 0;
        for (int i = 0; i < track.getSampleDurations().length; i++) {
            long delta = track.getSampleDurations()[i];

            if (Arrays.binarySearch(track.getSyncSamples(), currentSample + 1) >= 0) {
                // samples always start with 1 but we start with zero therefore +1
                timeOfSyncSamples[Arrays.binarySearch(track.getSyncSamples(), currentSample + 1)] = currentTime;
            }
            currentTime += (double) delta / (double) track.getTrackMetaData().getTimescale();
            currentSample++;

        }
        double previous = 0;
        for (double timeOfSyncSample : timeOfSyncSamples) {
            if (timeOfSyncSample > cutHere) {
                if (next) {
                    return timeOfSyncSample;
                } else {
                    return previous;
                }
            }
            previous = timeOfSyncSample;
        }
        return timeOfSyncSamples[timeOfSyncSamples.length - 1];
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is GoogleDocs.
     */
    public static boolean isGoogleDocs(Uri uri) {
        return "com.google.android.apps.docs.storage".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    public static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }
}
