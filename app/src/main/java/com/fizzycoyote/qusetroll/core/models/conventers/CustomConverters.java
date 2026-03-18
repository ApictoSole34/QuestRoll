package com.fizzycoyote.qusetroll.core.models.conventers;

import androidx.room.TypeConverter;

import com.fizzycoyote.qusetroll.core.models.custom.custom_character_class.custom_gained_at.CustomGainedAt;
import com.fizzycoyote.qusetroll.core.models.custom.custom_character_class.custom_table_data.CustomTableData;
import com.fizzycoyote.qusetroll.core.models.custom.custom_language.CustomLanguageEntity;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CustomConverters {
    @TypeConverter
    public static String fromScriptLanguage(CustomLanguageEntity language) {
        return language != null ? language.name : null;
    }

    @TypeConverter
    public static CustomLanguageEntity toScriptLanguage(String name) {
        return new CustomLanguageEntity(name, "");
    }

    /**
     * Custom Character Class
     */

    @TypeConverter
    public static String fromGainedAtList(List<CustomGainedAt> list) {
        return new Gson().toJson(list);
    }

    @TypeConverter
    public static List<CustomGainedAt> toGainedAtList(String json) {
        if (json == null) return Collections.emptyList();
        Type type = new TypeToken<List<CustomGainedAt>>(){}.getType();
        return new Gson().fromJson(json, type);
    }

    @TypeConverter
    public static String fromTableDataList(List<CustomTableData> list) {
        return new Gson().toJson(list);
    }

    @TypeConverter
    public static List<CustomTableData> toTableDataList(String json) {
        if (json == null) return Collections.emptyList();
        Type type = new TypeToken<List<CustomTableData>>(){}.getType();
        return new Gson().fromJson(json, type);
    }

    @TypeConverter
    public static List<String> toStringList(String json) {
        if (json == null) return new ArrayList<>();
        Type type = new TypeToken<List<String>>(){}.getType();
        return new Gson().fromJson(json, type);
    }

    @TypeConverter
    public static String fromStringList(List<String> list) {
        return new Gson().toJson(list);
    }
}
