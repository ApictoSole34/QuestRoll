package com.fizzycoyote.qusetroll.core.models.custom.custom_creature;

import android.os.Parcel;
import android.os.Parcelable;

public class CustomCreatureAction implements Parcelable {
    public String name = "";
    public String desc = "";
    public String actionType = "ACTION";

    public CustomCreatureAction() {}

    protected CustomCreatureAction(Parcel in) {
        name = in.readString();
        desc = in.readString();
        actionType = in.readString();
    }

    public static final Creator<CustomCreatureAction> CREATOR = new Creator<CustomCreatureAction>() {
        @Override public CustomCreatureAction createFromParcel(Parcel in) { return new CustomCreatureAction(in); }
        @Override public CustomCreatureAction[] newArray(int size) { return new CustomCreatureAction[size]; }
    };

    @Override public int describeContents() { return 0; }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(desc);
        dest.writeString(actionType);
    }
}