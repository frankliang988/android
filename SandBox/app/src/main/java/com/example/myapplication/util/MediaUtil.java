package com.example.myapplication.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.util.LongSparseArray;

import androidx.annotation.NonNull;

import com.example.myapplication.Events.GenerateVideoBitmapListEvent;

import org.greenrobot.eventbus.EventBus;

import java.io.File;

public class MediaUtil {

    /**
     * return time in milli seconds..
     * @param context
     * @param videoFile
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
}
