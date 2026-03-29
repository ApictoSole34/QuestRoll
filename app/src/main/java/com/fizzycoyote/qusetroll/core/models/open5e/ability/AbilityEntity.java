package com.fizzycoyote.qusetroll.core.models.open5e.ability;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "abilities")
public class AbilityEntity {
    @PrimaryKey
    @NonNull
    public String key;

    public String name;
    public String shortDesc;

    public String descriptionsJson;
}