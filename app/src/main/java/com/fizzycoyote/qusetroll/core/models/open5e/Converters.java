package com.fizzycoyote.qusetroll.core.models.open5e;

import androidx.room.TypeConverter;

import com.fizzycoyote.qusetroll.core.models.open5e.spell.CastingOptionDto;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

public class Converters {
    private static final Gson gson = new Gson();

    @TypeConverter
    public static String fromList(List<String> list) {
        return new Gson().toJson(list);
    }

    @TypeConverter
    public static List<String> toList(String json) {
        Type type = new TypeToken<List<String>>(){}.getType();
        return new Gson().fromJson(json,type);
    }

    @TypeConverter
    public static String fromCastingOptions(List<CastingOptionDto> list) {
        return list == null ? null : gson.toJson(list);
    }

    @TypeConverter
    public static List<CastingOptionDto> toCastingOptions(String json) {
        if (json == null) return null;
        Type listType = new TypeToken<List<CastingOptionDto>>() {
        }.getType();
        return gson.fromJson(json, listType);
    }
}
