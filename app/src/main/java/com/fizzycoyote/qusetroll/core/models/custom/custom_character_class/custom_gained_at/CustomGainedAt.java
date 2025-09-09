package com.fizzycoyote.qusetroll.core.models.custom.custom_character_class.custom_gained_at;

import android.os.Parcel;
import android.os.Parcelable;

public class CustomGainedAt implements Parcelable {
    public int level;
    public String details;

    public CustomGainedAt(int level, String details) {
        this.level = level;
        this.details = details;
    }

    protected CustomGainedAt(Parcel in) {
        level = in.readInt();
        details = in.readString();
    }

    public static final Creator<CustomGainedAt> CREATOR = new Creator<CustomGainedAt>() {
        @Override
        public CustomGainedAt createFromParcel(Parcel in) {
            return new CustomGainedAt(in);
        }

        @Override
        public CustomGainedAt[] newArray(int size) {
            return new CustomGainedAt[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(level);
        dest.writeString(details);
    }
}