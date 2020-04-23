package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;

import com.example.myapplication.media.VideoTrimmer.VideoTrimmerActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements MainExpandAdapter.ClickListener {
    public static final String TAG = MainActivity.class.getCanonicalName();

    private MainExpandAdapter mMainExpandAdapter;
    private MainViewModel mViewModel;
    private RecyclerView contactsList;
    private List<ExampleGroup> mDataList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null){
            getSupportActionBar().setTitle("Home");
        }
        mViewModel = new MainViewModel(getApplication());
        if(savedInstanceState == null){
            mDataList = mViewModel.initList();
        }
        contactsList = findViewById(R.id.contactsList);
        initAdapter();
    }

    private void initAdapter(){
        //initialize real view model here for live data
        if(mDataList != null && !mDataList.isEmpty()){
            mMainExpandAdapter = new MainExpandAdapter(this, mDataList, this);
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
            linearLayoutManager.setOrientation(RecyclerView.VERTICAL);
            contactsList.setLayoutManager(linearLayoutManager);
            contactsList.setAdapter(mMainExpandAdapter);
            int groupSize = mMainExpandAdapter.getGroups().size();

            if (mMainExpandAdapter.getGroups().size() > 0) {
                for (int i = groupSize - 1; i >=0; i--) {
                    if (!mMainExpandAdapter.isGroupExpanded(i))
                        mMainExpandAdapter.toggleGroup(i);
                }
            }
        }
    }

    public void itemClick(@NonNull ExampleItem item) {
        switch (item.category){
            case ExampleItem.CAT_LAYOUT: {
                break;
            }
            case ExampleItem.CAT_MEDIA: {
                switch (item.name){
                    case "Video Trimmer":
                        Intent intent = new Intent(this, VideoTrimmerActivity.class);
                        startActivity(intent);
                }
                break;
            }
        }
    }
}
