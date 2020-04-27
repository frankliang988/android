package com.example.myapplication.media.VideoTrimmer;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import com.example.myapplication.Constant;
import com.example.myapplication.Events.GenerateVideoBitmapListEvent;
import com.example.myapplication.util.MediaUtil;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;

import android.os.Environment;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.myapplication.R;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.IOException;

public class VideoTrimmerActivity extends AppCompatActivity implements TrimmerFragment.OnSaveVideo {
    public static final String TAG = VideoTrimmerActivity.class.getSimpleName();
    static final int REQUEST_VIDEO_CAPTURE = 1;
    static final String TAG_TRIMMER = "trimmer";

    private FloatingActionButton mBtnTakeVideo;
    private Uri mVideoUri;
    private VideoEntity mEntity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_trimmer);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }

        mBtnTakeVideo = findViewById(R.id.fab);
        mBtnTakeVideo.setOnClickListener(v -> takeVideoIfHasPermission());
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    private void takeVideoIfHasPermission() {
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.RECORD_AUDIO) +
                ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA},
                    Constant.RequestPermission.VIDEO);
        } else {
            dispatchTakeVideoIntent();
        }
    }

    private void dispatchTakeVideoIntent() {
        Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        if (takeVideoIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takeVideoIntent, REQUEST_VIDEO_CAPTURE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (grantResults.length > 0) {
            switch (requestCode) {
                case Constant.RequestPermission.VIDEO:

                    boolean audioPermission = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean cameraPermission = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if (audioPermission && cameraPermission) {
                        dispatchTakeVideoIntent();
                    } else {
                        if (!audioPermission && !cameraPermission) {
                            Toast.makeText(this, "Camera and Mic permission required", Toast.LENGTH_SHORT).show();
                        } else {
                            if (!audioPermission) {
                                Toast.makeText(this, "Mic permission required", Toast.LENGTH_SHORT).show();
                            }
                            if (!cameraPermission) {
                                Toast.makeText(this, "Camera permission required", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                    break;
                case Constant.RequestPermission.WRITE_EXTERNAL_STORAGE:
                    if(mEntity != null){
                        trimVideo(mEntity);
                    }
                    break;
            }

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (requestCode == REQUEST_VIDEO_CAPTURE && resultCode == RESULT_OK) {
            mVideoUri = intent.getData();
            String backStackTag = null;

            TrimmerFragment fragment = TrimmerFragment.newInstance(mVideoUri);
            if (getSupportFragmentManager().findFragmentByTag(TAG_TRIMMER) != null) {
                getSupportFragmentManager().popBackStack(TAG_TRIMMER, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                backStackTag = TAG_TRIMMER;
            }

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.media_send_fragment_container, fragment, TAG_TRIMMER)
                    .setCustomAnimations(R.anim.fade_in, R.anim.fade_out, R.anim.fade_in, R.anim.fade_out)
                    .addToBackStack(backStackTag)
                    .commit();

            if (mBtnTakeVideo != null) {
                mBtnTakeVideo.setVisibility(View.INVISIBLE);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(GenerateVideoBitmapListEvent event) {
        TrimmerFragment fragment = getTrimmerFragment();
        if (fragment != null) {
            fragment.onVideoBitmapListGenerated(event);
        }
    }

    @Override
    public void onBackPressed() {
        TrimmerFragment fragment = getTrimmerFragment();
        if (fragment != null && fragment.isVisible()) {
            if (mVideoUri != null) {
                //prevent memory leak
                getContentResolver().delete(mVideoUri, null, null);
            }
            mBtnTakeVideo.setVisibility(View.VISIBLE);
        }
        super.onBackPressed();
    }

    private @Nullable
    TrimmerFragment getTrimmerFragment() {
        return (TrimmerFragment) getSupportFragmentManager().findFragmentByTag(TAG_TRIMMER);
    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onClick(VideoEntity entity) {
        mEntity = entity;
        if (Build.VERSION.SDK_INT >= 23 && this.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    Constant.RequestPermission.WRITE_EXTERNAL_STORAGE);
        } else {
            trimVideo(entity);
        }
    }

    private void trimVideo(VideoEntity entity){
        String root = Environment.getExternalStorageDirectory().toString();
        File outputDirectory = new File(root, Environment.DIRECTORY_MOVIES);
        if (!outputDirectory.exists()) {
            outputDirectory.mkdirs();
        }
        try {
            String path = MediaUtil.trimVideo(entity.uri, outputDirectory, entity.startTimeMs,
                    entity.endTimeMs, getApplicationContext());
            Toast.makeText(this, "File saved at " + path, Toast.LENGTH_SHORT).show();
            onBackPressed();

        } catch (IOException e) {

        }
    }
}
