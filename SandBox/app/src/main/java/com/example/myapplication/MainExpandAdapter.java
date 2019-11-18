package com.example.myapplication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.thoughtbot.expandablerecyclerview.ExpandableRecyclerViewAdapter;
import com.thoughtbot.expandablerecyclerview.models.ExpandableGroup;

import java.util.List;

public class MainExpandAdapter extends ExpandableRecyclerViewAdapter<MainGroupTitleViewHolder, MainGroupItemViewHolder> {
    private Context mContext;
    private ClickListener clickListener;
    private List<ExampleGroup> groups;

    public MainExpandAdapter(Context context, List<ExampleGroup> groups , ClickListener listener){
        super(groups);
        this.mContext = context;
        this.clickListener = listener;
        this.groups = groups;
    }

    @Override
    public MainGroupTitleViewHolder onCreateGroupViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_example_header, parent, false);
        return new MainGroupTitleViewHolder(mContext, view);
    }

    @Override
    public void onBindGroupViewHolder(@NonNull MainGroupTitleViewHolder holder, int flatPosition, ExpandableGroup group) {
        holder.onBind((ExampleGroup) group, isGroupExpanded(flatPosition));
    }

    @Override
    public MainGroupItemViewHolder onCreateChildViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_example_row, parent, false);
        return new MainGroupItemViewHolder(mContext, view);
    }

    @Override
    public void onBindChildViewHolder(MainGroupItemViewHolder holder, int flatPosition, ExpandableGroup group, int childIndex) {
        final ExampleItem item = ((ExampleItem) group.getItems().get(childIndex));
        holder.onBind(item);
    }

    interface ClickListener {
        void itemClick(ExampleItem item);
    }

}
