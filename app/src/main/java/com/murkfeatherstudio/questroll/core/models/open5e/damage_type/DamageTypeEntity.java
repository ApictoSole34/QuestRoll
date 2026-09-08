package com.murkfeatherstudio.questroll.core.models.open5e.damage_type;


import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "damage_types")
public class DamageTypeEntity {
    @PrimaryKey
    @NonNull
    public String key;

    public String name;
    public String url;
    public String description;
    public String document;
}