package com.example.myapplication;

import android.os.Parcel;
import android.os.Parcelable;

public class ExampleItem implements Parcelable {
    public static final int CAT_SPECIAL = 0;
    public static final int CAT_LAYOUT = 1;
    public static final int CAT_MEDIA = 2;

    public int category;
    public String name;
    public int iconResId;

    public ExampleItem() {
        category = 1;
        name = "";
    }

    public ExampleItem(int category, String name, int iconResId){
        this.category = category;
        this.name = name;
        this.iconResId = iconResId;
    }

    public static final Creator<ExampleItem> CREATOR = new Creator<ExampleItem>() {
        @Override
        public ExampleItem createFromParcel(Parcel in) {
            return new ExampleItem(in);
        }

        @Override
        public ExampleItem[] newArray(int size) {
            return new ExampleItem[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(category);
        dest.writeString(this.name);
        dest.writeInt(this.iconResId);
    }

    private ExampleItem(Parcel in) {
        this.category = in.readInt();
        this.name = in.readString();
        this.iconResId = in.readInt();
    }
}
