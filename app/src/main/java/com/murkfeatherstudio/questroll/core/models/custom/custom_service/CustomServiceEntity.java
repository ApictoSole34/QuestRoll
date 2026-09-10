package com.murkfeatherstudio.questroll.core.models.custom.custom_service;

import androidx.room.ColumnInfo;
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

    @ColumnInfo(name = "game_system")
    public String gameSystem = "5e-2014";
}