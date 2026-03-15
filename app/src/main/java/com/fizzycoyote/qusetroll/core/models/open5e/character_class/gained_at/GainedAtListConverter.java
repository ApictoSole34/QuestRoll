package com.fizzycoyote.qusetroll.core.models.open5e.character_class.gained_at;

import android.util.Log;

import androidx.room.TypeConverter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GainedAtListConverter {
    private static final Gson gson = new Gson();

    @TypeConverter
    public static List<GainedAt> fromString(String value) {
        if (value == null || value.trim().isEmpty()) {
            return new ArrayList<>();
        }
        try {
            Type listType = new TypeToken<List<GainedAt>>(){}.getType();
            return gson.fromJson(value, listType);
        } catch (Exception e) {
            Log.e("GainedAtConverter", "Error parsing gainedAt", e);
            return new ArrayList<>();
        }
    }

    @TypeConverter
    public static String toString(List<GainedAt> list) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        return gson.toJson(list);
    }
}