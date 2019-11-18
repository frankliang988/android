package com.example.myapplication.util;

import android.app.Activity;
import android.content.Context;
import android.util.DisplayMetrics;
import android.util.TypedValue;

public class DisplayUtil {
    private final static String TAG = DisplayUtil.class.getSimpleName();

    public static int dpToPx(int dp, Context context) {
        float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
        return (int) px;
    }

    public static int getWidth(Context context) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity)context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics.widthPixels;
    }
}
