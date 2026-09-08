package com.murkfeatherstudio.questroll.core.models.custom.custom_environment;

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
}