package com.fizzycoyote.qusetroll.core.models.open5e.weapon_property;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "weapon_properties")
public class WeaponPropertyEntity {
    @PrimaryKey
    @NonNull
    public String key;
    public String name;
    public String desc;
    public String document;
    public String url;
    public String type;
}