package com.example.myapplication.media.VideoTrimmer;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.LongSparseArray;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.myapplication.Constant;
import com.example.myapplication.Events.GenerateVideoBitmapListEvent;
import com.example.myapplication.R;
import com.example.myapplication.util.FileUtil;
import com.example.myapplication.util.MediaUtil;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import java.util.List;
import java.util.Objects;

public class TrimmerFragment extends Fragment {

    public interface OnSaveVideo {
        public void onClick(VideoEntity entity);
    }

    private static final int LEFT = 1;
    private static final int RIGHT = 2;
    private static final int BOX = 3;
    private static final float ZOOM_IN_SCALE = 1.25f;
    private static final float ZOOM_OUT_SCALE = 1f;

    private int minimumTrimmingDistance = 100; //Max( Min(5 sec, video length) in dp, 100 dp)

    private Uri mUri;
    private SimpleExoPlayer mPlayer;
    private long mVideoDuration;
    private ImageButton mPlayButton;
    private TextView tvDuration;
    private TextView tvFileSize;

    private TimeLineView mTimeLineView;
    private TrimBoundaryContainerView mTrimBox; //The white container
    private ImageView mLeftIndex;           //left button
    private ImageView mRightIndex;          //Right button
    private int mCurrentIndex = 0;       //1: touching left index  2: right index, 0: the rectangle

    private float mLastXPosition;
    private float msPerPixel;
    private long startTimeMs = 0;
    private long endTimeMs;
    private long mFileSize;
    private boolean mWasPlaying = false;  //was video playing when start trimming
    private boolean mStopVideoHandler;    //switch to stop handler, stop video from playing
    private VideoEntity mMedia;

    private Handler mVideoProgressHandler; //handler to observe video progress

    private float lastX;

    private List<GenerateVideoBitmapListEvent> mEventList;

    private OnSaveVideo listener;

    //width of the button
    private float indexWidth = 0;

    public static TrimmerFragment newInstance(Uri uri) {
        TrimmerFragment fragment = new TrimmerFragment();
        Bundle args = new Bundle();
        args.putParcelable(Constant.Extra.URL, uri);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public @Nullable
    View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_trimmer, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mUri = requireArguments().getParcelable(Constant.Extra.URL);
        mMedia = new VideoEntity(mUri);
        mVideoProgressHandler = new Handler(Looper.getMainLooper());

        mTimeLineView = view.findViewById(R.id.timeLineView);
        if(mTimeLineView != null){
            mTimeLineView.setVideoUri(mUri);
        }

        mTrimBox = view.findViewById(R.id.trimBox);

        mVideoDuration = MediaUtil.getVideoDuration(requireContext(), mUri);
        endTimeMs = mVideoDuration;

        mLeftIndex = view.findViewById(R.id.left_index);
        mLeftIndex.setClickable(false);

        mRightIndex = view.findViewById(R.id.right_index);
        mRightIndex.setClickable(false);

        setTrimListener();

        PlayerView playerView = view.findViewById(R.id.player_view);
        mPlayer = ExoPlayerFactory.newSimpleInstance(requireContext());
        playerView.setPlayer(mPlayer);
        prepareVideo();
        tvDuration = view.findViewById(R.id.video_duration_Tv);
        tvFileSize = view.findViewById(R.id.file_size_Tv);
        if(mUri!= null){
            tvDuration.setText(MediaUtil.convertMillieToHMmSs(mVideoDuration));
            mFileSize = FileUtil.getFileSize(mUri, requireContext());
            tvFileSize.setText(FileUtil.formatFileSize(mFileSize));
        }
        mPlayButton = view.findViewById(R.id.playButton);

        playerView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        lastX = event.getX();
                        return true;

                    case MotionEvent.ACTION_UP:
                        toggleVideoPlay();
                        break;

                    case MotionEvent.ACTION_MOVE:
                        if(Math.abs(event.getX() - lastX) > 30){
                            return false;
                        }
                }
                return false;
            }
        });

        mPlayer.addListener(new Player.EventListener() {
            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                if (playWhenReady && playbackState == Player.STATE_ENDED) {
                    mPlayer.setPlayWhenReady(false);
                    showPlayButton();
                    mPlayer.getPlaybackState();
                    prepareVideo();
                }
            }
        });

        TextView tvSave = view.findViewById(R.id.tv_save);
        tvSave.setOnClickListener(v -> {
            if(listener != null){
                listener.onClick(mMedia);
            }
        });
    }

    @Override
    public void onDestroyView() {
        mVideoProgressHandler.removeCallbacks(updateProgressAction);
        mVideoProgressHandler = null;
        mPlayButton = null;
        mPlayer.release();
        super.onDestroyView();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        listener = (OnSaveVideo) context;
    }

    private void setTrimListener(){
        View.OnTouchListener listener = new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()){
                    case (MotionEvent.ACTION_DOWN):
                        //not allow parent view to intercept the event
                        v.getParent().requestDisallowInterceptTouchEvent(true);
                        detectTouchedView(event.getX());
                        mLastXPosition = event.getX();

                        if(mPlayer.getPlayWhenReady()){
                            mPlayer.setPlayWhenReady(false);
                            mStopVideoHandler = true;
                            mWasPlaying = true;
                        }
                        break;

                    case (MotionEvent.ACTION_CANCEL):
                    case (MotionEvent.ACTION_UP):
                        //if video was playing before trimming, resume playing
                        if(mWasPlaying){
                            mPlayer.setPlayWhenReady(true);
                            mStopVideoHandler = false;
                            updateProgress();
                            mWasPlaying = false;
                        }

                        //button UI change
                        if(mCurrentIndex == LEFT){
                            mLeftIndex.setScaleX(ZOOM_OUT_SCALE);
                            mLeftIndex.setScaleY(ZOOM_OUT_SCALE);
                        }else if(mCurrentIndex == RIGHT){
                            mRightIndex.setScaleX(ZOOM_OUT_SCALE);
                            mRightIndex.setScaleY(ZOOM_OUT_SCALE);
                        }

                        //save changes into media
                        mMedia.startTimeMs = startTimeMs;
                        mMedia.endTimeMs = endTimeMs;

                        if (startTimeMs > 0) {
                            mMedia.hasTrimmed = true;
                        } else if (0 < endTimeMs && endTimeMs < mVideoDuration) {
                            mMedia.hasTrimmed = true;
                        } else {
                            mMedia.hasTrimmed = false;
                        }
                        break;

                    case (MotionEvent.ACTION_MOVE):
                        float dx = event.getX() - mLastXPosition;
                        if(dx > 0){
                            //moving right
                            movingRight(dx);
                        }else {
                            //moving left
                            movingLeft(dx);
                        }
                        mLastXPosition = event.getX();
                        break;
                }

                return true;
            }
        };
        mTrimBox.setOnTouchListener(listener);
    }

    //When the gesture is moving left
    private void movingLeft(float dx){
        //if left button is already at the start
        boolean leftIndexBelowMin = mLeftIndex.getX() + indexWidth/2 <= mTimeLineView.getX();

        float moveAmount;  //in case moving too fast and dx is very big

        //do not allow the left button to move pass the boundary
        if(mLeftIndex.getX() + indexWidth/2 + dx < mTimeLineView.getX()){
            moveAmount = mTimeLineView.getX() - mLeftIndex.getX() - indexWidth/2; // a negative number
        }else {
            moveAmount = dx;
        }
        switch (mCurrentIndex){
            case LEFT:
                if(!leftIndexBelowMin){
                    mLeftIndex.setX(mLeftIndex.getX() + moveAmount);
                    updateTrimBox();
                }
                break;
            case RIGHT:
                //do not allow right button to move pass the minimum trimming distance
                if(mRightIndex.getX() - mLeftIndex.getX() > minimumTrimmingDistance){
                    // if user is moving too fast and the right button will violate the minimum distance, then
                    //only move the the minimum distance
                    if(mRightIndex.getX() + dx <= mLeftIndex.getX() + minimumTrimmingDistance){
                        mRightIndex.setX(mLeftIndex.getX() + minimumTrimmingDistance);
                    }else {
                        mRightIndex.setX(mRightIndex.getX() + dx);
                    }
                    updateTrimBox();
                }
                else {
                    if(!leftIndexBelowMin){
                        mRightIndex.setX(mRightIndex.getX() + moveAmount);
                        mLeftIndex.setX(mLeftIndex.getX() + moveAmount);
                        updateTrimBox();
                    }
                }
                break;

            case BOX:
                if(!leftIndexBelowMin){
                    mRightIndex.setX(mRightIndex.getX() + moveAmount);
                    mLeftIndex.setX(mLeftIndex.getX() + moveAmount);
                    updateTrimBox();
                }
                break;
        }
    }

    //similar to moveLeft
    private void movingRight(float dx){
        boolean rightIndexOverMax = mRightIndex.getX() + indexWidth/2 >= mTimeLineView.getX() + mTimeLineView.getWidth();
        float moveAmount;  //in case moving too fast and dx is very big
        if(mRightIndex.getX() + indexWidth/2 + dx > mTimeLineView.getX() + mTimeLineView.getWidth()){
            moveAmount = mTimeLineView.getX() + mTimeLineView.getWidth() - mRightIndex.getX() - indexWidth/2;
        }else {
            moveAmount = dx;
        }

        switch (mCurrentIndex) {
            case LEFT:
                if (mRightIndex.getX() - mLeftIndex.getX() > minimumTrimmingDistance) {
                    if(mLeftIndex.getX() + dx >= mRightIndex.getX() - minimumTrimmingDistance){
                        mLeftIndex.setX(mRightIndex.getX() - minimumTrimmingDistance);
                    }else {
                        mLeftIndex.setX(mLeftIndex.getX() + dx);
                    }
                    updateTrimBox();
                } else {
                    if (!rightIndexOverMax) {
                        mRightIndex.setX(mRightIndex.getX() + moveAmount);
                        mLeftIndex.setX(mLeftIndex.getX() + moveAmount);
                        updateTrimBox();
                    }
                }
                break;

            case RIGHT:
                if (!rightIndexOverMax) {
                    mRightIndex.setX(mRightIndex.getX() + moveAmount);
                    updateTrimBox();
                }
                break;

            case BOX:
                if (!rightIndexOverMax) {
                    mRightIndex.setX(mRightIndex.getX() + moveAmount);
                    mLeftIndex.setX(mLeftIndex.getX() + moveAmount);
                    updateTrimBox();
                }
                break;
        }
    }

    // detect if user is touching the left button, right button, or the trim box
    private void detectTouchedView(float x){
        if(x > mLeftIndex.getX() - 20 && x < mLeftIndex.getX() + indexWidth + 20){
            mCurrentIndex = LEFT;
            mLeftIndex.setScaleX(ZOOM_IN_SCALE);
            mLeftIndex.setScaleY(ZOOM_IN_SCALE);
        }else if(x > mRightIndex.getX()- 20 && x < mRightIndex.getX() + indexWidth+ 20){
            mCurrentIndex = RIGHT;
            mRightIndex.setScaleX(ZOOM_IN_SCALE);
            mRightIndex.setScaleY(ZOOM_IN_SCALE);
        }else if(x > mLeftIndex.getX() + indexWidth + 20 && x < mRightIndex.getX() - 20){
            mCurrentIndex = BOX;
        }else {
            mCurrentIndex = 0;
        }
    }

    //First time drawing each trimming related view component
    private void initTrimContainer(){
        msPerPixel = (float) mVideoDuration / (float) mTimeLineView.getWidth();
        minimumTrimmingDistance = Math.max(minimumTrimmingDistance, (int)durationToCoordinate(Math.min(5000, mVideoDuration)));

        indexWidth = mLeftIndex.getWidth();

        float left = mTimeLineView.getX() - indexWidth/2;
        float right = mTimeLineView.getX() + mTimeLineView.getWidth() - indexWidth/2;

        //if the media has already been trimmed before ---- restore the state

        if (mMedia.hasTrimmed) {
            left = mTimeLineView.getX() + durationToCoordinate(mMedia.startTimeMs) - indexWidth / 2;
            right = mTimeLineView.getX() + durationToCoordinate(mMedia.endTimeMs) - indexWidth / 2;
        }


        mLeftIndex.setX(left);
        mLeftIndex.setVisibility(View.VISIBLE);

        mRightIndex.setX(right);
        mRightIndex.setVisibility(View.VISIBLE);

        updateTrimBox();
        mTrimBox.setVisibility(View.VISIBLE);
    }

    //Redraw the trim box on left/right button move, and make player seek to the starting position
    private void updateTrimBox(){
        startTimeMs = coordinateToDuration(mLeftIndex.getX() + indexWidth/2 - mTimeLineView.getX());
        endTimeMs = coordinateToDuration(mRightIndex.getX() + indexWidth/2 - mTimeLineView.getX());
        tvDuration.setText(MediaUtil.getDurationText(Math.max(0, endTimeMs-startTimeMs)));
        tvFileSize.setText(FileUtil.formatFileSize(Math.max(0, (endTimeMs - startTimeMs)*mFileSize/mVideoDuration)));

        mTrimBox.setRect(mTimeLineView.getX(), mTimeLineView.getX() + mTimeLineView.getWidth(),
                (mLeftIndex.getX() + indexWidth/2), mTimeLineView.getY(),
                mRightIndex.getX() + indexWidth/2, mTimeLineView.getY() + mTimeLineView.getHeight());

        if(mPlayer != null){
            mPlayer.seekTo(startTimeMs);
        }
    }

    //Trans form a coordinate value to the corresponding time of video
    private long coordinateToDuration(float x){
        return (long)(x * msPerPixel);
    }

    private float durationToCoordinate(long ms){
        return ms / msPerPixel;
    }

    /**
     * This and updateProgressAction forms an infinite loop until mStopVideoHandler become true.
     * It is use to check video progress every 300 ms to check when to stop video.
     */
    private void updateProgress(){
        long position = mPlayer == null ? 0 : mPlayer.getCurrentPosition();
        float progressXCoord = mTimeLineView.getX() + durationToCoordinate(position);
        if(progressXCoord > mRightIndex.getX()+indexWidth/2){
            progressXCoord = mRightIndex.getX()+indexWidth/2;
        }

        if(progressXCoord < mLeftIndex.getX() + indexWidth/2){
            progressXCoord = mLeftIndex.getX() + indexWidth/2;
        }

        mTrimBox.updateProgressLine(progressXCoord,mTimeLineView.getY(),mTimeLineView.getY() + mTimeLineView.getHeight());
        if(position >= endTimeMs){
            if(mPlayer.getPlayWhenReady()){
                mPlayer.setPlayWhenReady(false);
                mPlayer.seekTo(startTimeMs);
                showPlayButton();
                mStopVideoHandler = true;
            }
        }
        if (mVideoProgressHandler != null) {
            mVideoProgressHandler.postDelayed(updateProgressAction, 100);
        }
    }

    private final Runnable updateProgressAction = new Runnable() {
        @Override
        public void run() {
            if(!mStopVideoHandler){
                updateProgress();
            }
        }
    };

    private void prepareVideo(){
        DefaultDataSourceFactory dataSourceFactory = new DefaultDataSourceFactory(
                Objects.requireNonNull(this.getContext()), Util.getUserAgent(this.getContext(), getContext().getPackageName()));
        MediaSource videoSource = new ProgressiveMediaSource.Factory(dataSourceFactory)
                .createMediaSource(mUri);
        mPlayer.prepare(videoSource);
    }

    private void toggleVideoPlay(){
        if(mPlayer.getPlayWhenReady()){
            mPlayer.setPlayWhenReady(false);
            mPlayer.getPlaybackState();
            showPlayButton();
            mStopVideoHandler = true;
        }else {
            mPlayer.setPlayWhenReady(true);
            mPlayer.getPlaybackState();
            hidePlayButton();
            mStopVideoHandler = false;
            updateProgress();
        }
    }

    private void showPlayButton(){
        if (mPlayButton != null) {
            mPlayButton.animate()
                    .alpha(1f)
                    .setDuration(getResources().getInteger(
                            android.R.integer.config_shortAnimTime))
                    .setListener(null);
        }
    }

    private void hidePlayButton(){
        if (mPlayButton != null) {
            mPlayButton.animate()
                    .alpha(0f)
                    .setDuration(getResources().getInteger(
                            android.R.integer.config_shortAnimTime))
                    .setListener(null);
        }
    }

    public void onVideoBitmapListGenerated(GenerateVideoBitmapListEvent event) {
        reDrawTimeline(event.thumbnailList);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mTimeLineView.setVisibility(View.INVISIBLE);
        mLeftIndex.setVisibility(View.INVISIBLE);
        mTrimBox.setVisibility(View.INVISIBLE);
        mRightIndex.setVisibility(View.INVISIBLE);
    }

    public void reDrawTimeline(LongSparseArray<Bitmap> list) {
        if (mTimeLineView != null) {
            mTimeLineView.mBitmapList = list;
            //redraw the timeline to fill with bitmat
            mTimeLineView.invalidate();
            mTimeLineView.setVisibility(View.VISIBLE);
            initTrimContainer();
        }
    }
}
