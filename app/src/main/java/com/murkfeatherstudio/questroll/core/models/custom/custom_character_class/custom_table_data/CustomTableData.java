package com.murkfeatherstudio.questroll.core.models.custom.custom_character_class.custom_table_data;

import android.os.Parcel;
import android.os.Parcelable;

public class CustomTableData implements Parcelable {
    public int level;
    public String columnValue;

    public CustomTableData(int level, String columnValue) {
        this.level = level;
        this.columnValue = columnValue;
    }

    protected CustomTableData(Parcel in) {
        level = in.readInt();
        columnValue = in.readString();
    }

    public static final Creator<CustomTableData> CREATOR = new Creator<CustomTableData>() {
        @Override
        public CustomTableData createFromParcel(Parcel in) {
            return new CustomTableData(in);
        }

        @Override
        public CustomTableData[] newArray(int size) {
            return new CustomTableData[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(level);
        dest.writeString(columnValue);
    }
}