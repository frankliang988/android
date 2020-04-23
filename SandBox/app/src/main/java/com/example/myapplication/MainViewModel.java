package com.example.myapplication;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import java.util.ArrayList;
import java.util.List;
class MainViewModel extends AndroidViewModel {
    private static final int INFO_SECTION_IDX = 0;
    private static final int MEDIA_SECTION_IDX = 1;
    private static final int LAYOUT_SECTION_IDX = 2;

    private static final int TOTAL_SECTIONS = 3;

    private List<ExampleGroup> mBaseList = new ArrayList<>();

    MainViewModel(@NonNull Application application) {
        super(application);
    }

    List<ExampleGroup> initList(){
        initBaseSectionList();
        initTopItems();

        List<ExampleItem> layoutItems = new ArrayList<>();
        layoutItems.add(new ExampleItem(ExampleItem.CAT_LAYOUT, "Grid Layout", R.drawable.ic_view_quilt));
        layoutItems.add(new ExampleItem(ExampleItem.CAT_LAYOUT, "Tab Layout",R.drawable.ic_view_quilt));
        layoutItems.add(new ExampleItem(ExampleItem.CAT_LAYOUT, "Recycler List", R.drawable.ic_view_quilt));
        ExampleGroup layoutGroup = new ExampleGroup("Layout Example",
                layoutItems, LAYOUT_SECTION_IDX, true);


        List<ExampleItem> mediaItems = new ArrayList<>();
        mediaItems.add(new ExampleItem(ExampleItem.CAT_MEDIA, "Location Picker", R.drawable.ic_my_location));
        mediaItems.add(new ExampleItem(ExampleItem.CAT_MEDIA, "Map Generation",R.drawable.ic_map));
        mediaItems.add(new ExampleItem(ExampleItem.CAT_MEDIA, "Gallery", R.drawable.ic_image));
        mediaItems.add(new ExampleItem(ExampleItem.CAT_MEDIA, "Video Trimmer",R.drawable.ic_video_library));
        ExampleGroup mediaGroup = new ExampleGroup("Media Example",
                mediaItems, MEDIA_SECTION_IDX, true);

        mBaseList.set(LAYOUT_SECTION_IDX, layoutGroup);
        mBaseList.set(MEDIA_SECTION_IDX, mediaGroup);

        return mBaseList;

    }

    private void initTopItems(){
        List<ExampleItem> topItems = new ArrayList<>();
        String title = Constant.Unknown.EMPTY_STRING;

        topItems.add(new ExampleItem(ExampleItem.CAT_SPECIAL, "Info", R.drawable.ic_info));
        topItems.add(new ExampleItem(ExampleItem.CAT_SPECIAL, "Settings", R.drawable.ic_settings));

        mBaseList.set(INFO_SECTION_IDX, new ExampleGroup(title, topItems, INFO_SECTION_IDX, false));
    }


    private void initBaseSectionList() {
        mBaseList = new ArrayList<>();

        for (int i = 0; i < TOTAL_SECTIONS; i++) {
            mBaseList.add(new ExampleGroup(Constant.Unknown.EMPTY_STRING, null, i, false));
        }
    }
}
