package com.fizzycoyote.qusetroll.core.models.custom.custom_service;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "custom_services")
public class CustomServiceEntity {
    @PrimaryKey(autoGenerate = true)
    public long id;

    public String key;
    public String name;
    public String desc;
    public String cost;
    public String detail;
}