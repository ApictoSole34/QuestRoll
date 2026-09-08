package com.murkfeatherstudio.questroll.core.models.open5e.character_class.table_data;

import android.util.Log;

import androidx.room.TypeConverter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class TableDataListConverter {
    private static final Gson gson = new Gson();

    @TypeConverter
    public static List<TableData> fromString(String value) {
        if (value == null || value.trim().isEmpty()) {
            return new ArrayList<>();
        }
        try {
            Type listType = new TypeToken<List<TableData>>(){}.getType();
            return gson.fromJson(value, listType);
        } catch (Exception e) {
            Log.e("TableDataConverter", "Error parsing tableData", e);
            return new ArrayList<>();
        }
    }

    @TypeConverter
    public static String toString(List<TableData> list) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        return gson.toJson(list);
    }
}