package com.example.myapplication.media.VideoTrimmer;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import com.example.myapplication.Constant;
import com.example.myapplication.Events.GenerateVideoBitmapListEvent;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;

import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.myapplication.R;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.security.Permissions;

public class VideoTrimmerActivity extends AppCompatActivity {
    public static final String TAG = VideoTrimmerActivity.class.getSimpleName();
    static final int REQUEST_VIDEO_CAPTURE = 1;
    static final String TAG_TRIMMER = "trimmer";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_trimmer);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                takeVideoIfHasPermission();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    private void takeVideoIfHasPermission(){
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.RECORD_AUDIO) +
                ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA},
                    Constant.RequestPermission.VIDEO);
        }else {
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
            boolean audioPermission = grantResults[0] == PackageManager.PERMISSION_GRANTED;
            boolean cameraPermission = grantResults[1] == PackageManager.PERMISSION_GRANTED;
            if(audioPermission && cameraPermission){
                dispatchTakeVideoIntent();
            }else {
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
        }else {
            Toast.makeText(this, "Camera and Mic permission required", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (requestCode == REQUEST_VIDEO_CAPTURE && resultCode == RESULT_OK) {
            Uri videoUri = intent.getData();
            String backStackTag = null;

            TrimmerFragment fragment = TrimmerFragment.newInstance(videoUri);
            if (getSupportFragmentManager().findFragmentByTag(TAG_TRIMMER) != null) {
                getSupportFragmentManager().popBackStack(TAG_TRIMMER, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                backStackTag = TAG_TRIMMER;
            }

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.media_send_fragment_container, fragment, TAG_TRIMMER)
                    .setCustomAnimations(R.anim.fade_in, R.anim.fade_out, R.anim.fade_in, R.anim.fade_out)
                    .addToBackStack(backStackTag)
                    .commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(GenerateVideoBitmapListEvent event){
        TrimmerFragment fragment = getTrimmerFragment();
        if(fragment != null){
            fragment.onVideoBitmapListGenerated(event);
        }
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
}
