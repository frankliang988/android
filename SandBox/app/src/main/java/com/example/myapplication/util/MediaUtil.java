package com.example.myapplication.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.AsyncTask;
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
    public static long  getVideoDuration(@NonNull Context context, String videoFile) {
        try {
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(context, Uri.fromFile(new File(videoFile)));
            String time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            long timeInMillisec = Long.parseLong(time );
            retriever.release();

            return timeInMillisec;
        }catch (Exception e) { }

        return 0;
    }

    public static void generateThumbnailBitmapList(long videoDurationMs, Uri uri, int containerWidth, int bitmapDimen){
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    if(FileUtil.isExist(uri.getPath())){
                        LongSparseArray<Bitmap> bitmapList = new LongSparseArray<>();

                        //get video duration and parse into micro sec
                        long videoDurationMicos = videoDurationMs * 1000;

                        //calculate the # of bitmaps needed
                        int bitmapCount = (int) Math.ceil(((float) containerWidth) / bitmapDimen);

                        MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
                        mediaMetadataRetriever.setDataSource(uri.getPath());

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
                    }
                } catch (final Throwable e){
                    Thread.getDefaultUncaughtExceptionHandler().uncaughtException(Thread.currentThread(), e);
                }
            }
        });
    }
}
