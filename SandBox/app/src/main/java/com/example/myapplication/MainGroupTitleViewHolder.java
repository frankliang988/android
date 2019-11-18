package com.example.myapplication;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.myapplication.util.DisplayUtil;
import com.thoughtbot.expandablerecyclerview.viewholders.GroupViewHolder;

public class MainGroupTitleViewHolder extends GroupViewHolder {
    private Context mContext;
    private View view;
    private TextView mtvHeader;
    private ImageButton mExpandBtn;


    public MainGroupTitleViewHolder(@NonNull Context context, View itemView) {
        super(itemView);
        this.mContext = context;
        this.view = itemView;
        mtvHeader = itemView.findViewById(R.id.tv_group_title);
        mExpandBtn = itemView.findViewById(R.id.btn_expand);

    }

    @SuppressLint("SetTextI18n")
    public void onBind(@Nullable ExampleGroup group, Boolean isExpanded) {
        if (group != null) {

            if (!group.showTitle) {
                view.setLayoutParams(new LinearLayout.LayoutParams(0, 0));
            } else {
                String strHeader = group.getTitle() ;
                if (group.getItems() != null ) {
                    strHeader = strHeader + " (" + group.getItems().size() + ")";
                }
                mtvHeader.setText(strHeader);
                view.setLayoutParams(new LinearLayout.LayoutParams(DisplayUtil.getWidth(mContext), DisplayUtil.dpToPx(36, mContext)));
            }
            if(isExpanded){
                changeIconOnExpand();
            }else {
                changeIconOnHide();
            }
        }
    }

    @Override
    public void expand() {
        changeIconOnExpand();
    }

    @Override
    public void collapse() {
        changeIconOnHide();
    }

    void changeIconOnExpand(){
        mExpandBtn.setImageResource(R.drawable.ic_up_arrow);
    }

    void changeIconOnHide(){
        mExpandBtn.setImageResource(R.drawable.ic_down_arrow);
    }

}
