package com.fizzycoyote.qusetroll.core.models.custom.custom_spell;

import android.os.Parcel;
import android.os.Parcelable;

public class CustomCastingOption implements Parcelable {
    public String type = "";
    public String damageRoll = "";
    public String range = "";
    public String duration = "";
    public String desc = "";

    public CustomCastingOption() {}

    protected CustomCastingOption(Parcel in) {
        type = in.readString();
        damageRoll = in.readString();
        range = in.readString();
        duration = in.readString();
        desc = in.readString();
    }

    public static final Creator<CustomCastingOption> CREATOR = new Creator<CustomCastingOption>() {
        @Override
        public CustomCastingOption createFromParcel(Parcel in) {
            return new CustomCastingOption(in);
        }
        @Override
        public CustomCastingOption[] newArray(int size) {
            return new CustomCastingOption[size];
        }
    };

    @Override
    public int describeContents() { return 0; }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(type);
        dest.writeString(damageRoll);
        dest.writeString(range);
        dest.writeString(duration);
        dest.writeString(desc);
    }
}