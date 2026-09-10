package com.murkfeatherstudio.questroll.core.models.custom.custom_environment;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "custom_environments")
public class CustomEnvironmentEntity {
    @PrimaryKey(autoGenerate = true)
    public long id;

    public String key;
    public String name;
    public String desc;
    public boolean aquatic;
    public boolean planar;
    public boolean interior;

    @ColumnInfo(name = "game_system")
    public String gameSystem = "5e-2014";
}