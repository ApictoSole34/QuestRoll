package com.fizzycoyote.qusetroll.core.models.open5e.language;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "languages")
public class LanguageEntity {
    @PrimaryKey @NonNull public String key;
    public String url;
    public String document;
    public String name;
    public String desc;
    public boolean isExotic;
    public boolean isSecret;
    @Nullable public String scriptLanguage;
}
