package com.example.myapplication.media.VideoTrimmer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.LongSparseArray;
import android.view.View;

import androidx.annotation.NonNull;

import com.example.myapplication.R;
import com.example.myapplication.util.MediaUtil;

public class TimeLineView extends View {
    private Uri mVideoUri;
    private int mHeightView;
    public Rect trimBox;
    public LongSparseArray<Bitmap> mBitmapList = null;

    public TimeLineView(@NonNull Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    private TimeLineView(@NonNull Context context, AttributeSet attrs, int defStylerAttr) {
        super(context, attrs, defStylerAttr);
        mHeightView = getContext().getResources().getDimensionPixelOffset(R.dimen.time_line_height);
        this.trimBox = new Rect(0,0,0,0);
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int minW = getPaddingLeft() + getPaddingRight() + getSuggestedMinimumWidth();
        int w = resolveSizeAndState(minW, widthMeasureSpec, 1);

        final int minH = getPaddingBottom() + getPaddingTop() + mHeightView;
        int h = resolveSizeAndState(minH, heightMeasureSpec, 1);

        setMeasuredDimension(w, h);
    }

    @Override
    protected void onSizeChanged(final int w, int h, final int oldW, int oldH) {
        super.onSizeChanged(w, h, oldW, oldH);
        if(w != oldW) {
            getBitmap(w);
        }
    }


    public void setVideoUri(Uri uri) {
        this.mVideoUri = uri;
    }

    private void getBitmap(final int viewWidth) {
        if(mVideoUri != null){
            MediaUtil.generateThumbnailBitmapList(MediaUtil.getVideoDuration(getContext(), mVideoUri.getPath()),
                    mVideoUri, viewWidth, mHeightView);
        }
    }


    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);

        if (mBitmapList != null ) {
            canvas.save();
            int x = 0;

            for (int i = 0; i < mBitmapList.size(); i++) {
                Bitmap bitmap = mBitmapList.get(i);

                if (bitmap != null) {
                    canvas.drawBitmap(bitmap, x, 0, null);
                    x = x + bitmap.getWidth();
                }
            }
        }
    }

}
