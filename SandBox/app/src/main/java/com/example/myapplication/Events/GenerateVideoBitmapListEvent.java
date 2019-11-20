package com.example.myapplication.Events;

import android.graphics.Bitmap;
import android.net.Uri;
import android.util.LongSparseArray;

public class GenerateVideoBitmapListEvent {
    public Uri videoUri;
    public LongSparseArray<Bitmap> thumbnailList;

    public GenerateVideoBitmapListEvent(Uri uri, LongSparseArray<Bitmap> thumbnailList){
        this.videoUri = uri;
        this.thumbnailList = thumbnailList;
    }
}
