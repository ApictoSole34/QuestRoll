package com.fizzycoyote.qusetroll.core.models.open5e;

import androidx.room.TypeConverter;

import com.fizzycoyote.qusetroll.core.models.open5e.character_class.gained_at.GainedAtDto;
import com.fizzycoyote.qusetroll.core.models.open5e.character_class.table_data.TableDataDto;
import com.fizzycoyote.qusetroll.core.models.open5e.spell.CastingOptionDto;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.Collections;
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
    public static String fromIntegerList(List<Integer> list) {
        return gson.toJson(list);
    }

    @TypeConverter
    public static List<Integer> toIntegerList(String json) {
        return gson.fromJson(json, new TypeToken<List<Integer>>(){}.getType());
    }

    /**
     * Character class feature converters
     */
    @TypeConverter
    public static String gainedAtListToJson(List<GainedAtDto> list) {
        return gson.toJson(list);
    }

    @TypeConverter
    public static List<GainedAtDto> jsonToGainedAtList(String json) {
        if (json == null) return Collections.emptyList();
        Type type = new TypeToken<List<GainedAtDto>>() {}.getType();
        return gson.fromJson(json, type);
    }

    @TypeConverter
    public static String tableDataListToJson(List<TableDataDto> list) {
        return gson.toJson(list);
    }

    @TypeConverter
    public static List<TableDataDto> jsonToTableDataList(String json) {
        if (json == null) return Collections.emptyList();
        Type type = new TypeToken<List<TableDataDto>>() {}.getType();
        return gson.fromJson(json, type);
    }


}
