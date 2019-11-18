package com.example.myapplication;

import android.content.Context;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.bumptech.glide.Glide;
import com.thoughtbot.expandablerecyclerview.viewholders.ChildViewHolder;

public class MainGroupItemViewHolder extends ChildViewHolder {

    private Context mContext;
    private ImageView avatar;
    private TextView tvDialogName;
    private ConstraintLayout mRootLayout;
    private TextView tvDialogHint;

    MainGroupItemViewHolder(Context context, View itemView) {
        super(itemView);
        this.mContext = context;
        tvDialogName = itemView.findViewById(R.id.dialogName);
        tvDialogHint = itemView.findViewById(R.id.dialog_hint);
        avatar = itemView.findViewById(R.id.dialogAvatar);
        mRootLayout = itemView.findViewById(R.id.dialogRootLayout);

    }

    public void onBind(ExampleItem item) {
        tvDialogName.setText(item.name);
        tvDialogName.setTextSize(TypedValue.COMPLEX_UNIT_SP,18);
        tvDialogHint.setVisibility(View.GONE);
        Glide.with(mContext).load(item.iconResId).into(avatar);
    }
}
