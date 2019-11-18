package com.example.myapplication;

import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.RelativeLayout;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements MainExpandAdapter.ClickListener {
    public static final String TAG = MainActivity.class.getCanonicalName();

    private MainExpandAdapter mMainExpandAdapter;
    private RelativeLayout mLayoutContactList;
    private RecyclerView contactsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        contactsList = findViewById(R.id.contactsList);
        mLayoutContactList = findViewById(R.id.layoutContactList);
        initAdapter();
    }

    private void initAdapter(){
        //initialize real view model here for live data
        MainViewModel viewModel = new MainViewModel(getApplication());
        mMainExpandAdapter = new MainExpandAdapter(this, viewModel.initList(), this);

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


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void itemClick(@NonNull ExampleItem item) {

    }
}
