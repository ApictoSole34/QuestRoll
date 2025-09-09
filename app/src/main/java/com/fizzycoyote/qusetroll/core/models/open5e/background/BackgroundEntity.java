package com.fizzycoyote.qusetroll.core.models.open5e.background;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.fizzycoyote.qusetroll.core.models.open5e.Converters;

import java.util.List;

@Entity(tableName = "backgrounds")
@TypeConverters(Converters.class)
public class BackgroundEntity {
    @PrimaryKey @NonNull public String key;
    public String url;
    public String document;
    public List<String> benefitTypes;
    public List<String> benefitNames;
    public List<String> benefitDescs;
    public String name;
    @Nullable public String desc;
}

