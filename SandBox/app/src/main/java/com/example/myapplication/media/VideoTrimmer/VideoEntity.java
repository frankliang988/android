package com.example.myapplication.media.VideoTrimmer;

import android.net.Uri;

public class VideoEntity {
    Uri uri;
    boolean hasTrimmed;
    long startTimeMs;
    long endTimeMs;

    public VideoEntity(Uri uri){
        this.uri = uri;
    }
}
