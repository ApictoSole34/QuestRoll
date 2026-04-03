package com.fizzycoyote.qusetroll.core.models.custom.custom_creature_type;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "custom_creature_types")
public class CustomCreatureTypeEntity {
    @PrimaryKey(autoGenerate = true)
    public long id;
    @NonNull
    public String name;
    public String description;
}