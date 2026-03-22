package com.fizzycoyote.qusetroll.core.models.custom.custom_background;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "custom_backgrounds")
public class CustomBackgroundEntity {

    @PrimaryKey(autoGenerate = true)
    public long id;

    @NonNull
    public String name = "";
    public String desc = "";

    @ColumnInfo(name = "benefits_json") public String benefitsJson = "";
}

