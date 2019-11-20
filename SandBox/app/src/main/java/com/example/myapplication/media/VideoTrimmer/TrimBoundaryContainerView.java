package com.example.myapplication.media.VideoTrimmer;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;

import com.example.myapplication.R;

public class TrimBoundaryContainerView extends View {
    private int mHeightView;
    public Rect trimBox;
    public Rect leftShade;
    public Rect rightShade;
    public Line progressLine;
    Paint strokePaint = new Paint();
    Paint fillPaint = new Paint();
    Paint linePaint = new Paint();

    public TrimBoundaryContainerView(@NonNull Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    private TrimBoundaryContainerView(@NonNull Context context, AttributeSet attrs, int defStylerAttr) {
        super(context, attrs, defStylerAttr);
        mHeightView = getContext().getResources().getDimensionPixelOffset(R.dimen.time_line_height);
        this.trimBox = new Rect(0,0,0,0);
        this.leftShade = new Rect(0, 0, 0, 0);
        this.rightShade = new Rect(0, 0, 0, 0);
        this.progressLine = new Line(0, 0, 0);
        initPaints();
    }

    void initPaints() {
        fillPaint.setStyle(Paint.Style.FILL);
        fillPaint.setColor(getResources().getColor(R.color.dark_gray_transparent));

        strokePaint.setStyle(Paint.Style.STROKE);
        strokePaint.setColor(getResources().getColor(R.color.white));
        strokePaint.setStrokeWidth(5);

        linePaint.setColor(getResources().getColor(R.color.white));
        linePaint.setStrokeWidth(4);
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
    }

    public void setRect(float start, float end, float x, float y, float width, float height){
        this.trimBox.set((int)x,(int)y + 2,(int)width,(int) height-2);
        this.leftShade.set((int) start, (int)y, Math.max((int)x-2, (int) start), (int)height);
        this.rightShade.set((int) width+2, (int)y, (int)end, (int)height);
        resetProgress();
        invalidate();
    }

    private void resetProgress(){
        this.progressLine.set((float)trimBox.left, (float)trimBox.top, (float)trimBox.bottom);
    }

    public void updateProgressLine(float x, float y, float height){
        this.progressLine.set(x, y, height);

        invalidate();
    }

    @Override
    public boolean performClick(){
        return false;
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawRect(trimBox, strokePaint);  // stroke
        canvas.drawRect(leftShade, fillPaint);
        canvas.drawRect(rightShade, fillPaint);
        canvas.drawLine(progressLine.x, progressLine.y, progressLine.x, progressLine.height, linePaint);
    }

    private class Line {
        float x;
        float y;
        float height;

        public Line(float x, float y, float height){
            this.x = x;
            this.y = y;
            this.height = height;
        }

        public void set(float x, float y, float height){
            this.x = x;
            this.y = y;
            this.height = height;
        }
    }

}
