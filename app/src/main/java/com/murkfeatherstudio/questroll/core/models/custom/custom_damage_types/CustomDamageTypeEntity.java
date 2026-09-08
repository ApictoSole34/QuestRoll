package com.murkfeatherstudio.questroll.core.models.custom.custom_damage_types;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "custom_damage_types")
public class CustomDamageTypeEntity {
    @PrimaryKey(autoGenerate = true)
    public long id;
    public String name;
    public String description;
}