package com.fizzycoyote.qusetroll.core.models.open5e.damage_type;


import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "damage_types")
public class DamageTypeEntity {
    @PrimaryKey @NonNull public String key;
    public String url;
    public String name;
    public String desc;
    public String documentUrl;
}