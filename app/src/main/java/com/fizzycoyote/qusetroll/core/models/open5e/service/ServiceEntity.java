package com.fizzycoyote.qusetroll.core.models.open5e.service;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "services")
public class ServiceEntity {
    @PrimaryKey
    @NonNull
    public String key;
    public String name;
    public String desc;
    public String cost;
    public String detail;
    public String url;
    @ColumnInfo(name = "document_name") public String documentName;
    @ColumnInfo(name = "document_key") public String documentKey;
}