package com.fizzycoyote.qusetroll.core.models.custom;

import androidx.room.TypeConverter;

import com.fizzycoyote.qusetroll.core.models.custom.custom_language.CustomLanguageEntity;

public class CustomConverters {
    @TypeConverter
    public static String fromScriptLanguage(CustomLanguageEntity language) {
        return language != null ? language.name : null;
    }

    @TypeConverter
    public static CustomLanguageEntity toScriptLanguage(String name) {
        return new CustomLanguageEntity(name, "");
    }
}
