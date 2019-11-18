package com.example.myapplication;

import com.thoughtbot.expandablerecyclerview.models.ExpandableGroup;

import java.util.ArrayList;
import java.util.List;

public class ExampleGroup extends ExpandableGroup<ExampleItem> {
    public int mSectionIdx;
    public boolean showTitle;

    public ExampleGroup(String title, List<ExampleItem> items, int sectionIdx, boolean showTitle){
        super(title, items);
        this.mSectionIdx = sectionIdx;
        this.showTitle = showTitle;
    }

    public ExampleGroup clearAndCopy() {
        return new ExampleGroup(this.getTitle(), new ArrayList<ExampleItem>(), mSectionIdx, showTitle);
    }
}
