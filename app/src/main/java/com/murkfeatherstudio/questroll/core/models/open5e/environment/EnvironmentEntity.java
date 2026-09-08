package com.murkfeatherstudio.questroll.core.models.open5e.environment;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "environments")
public class EnvironmentEntity {
    @PrimaryKey
    @NonNull
    public String key;
    public String name;
    public String desc;
    public boolean aquatic;
    public boolean planar;
    public boolean interior;
    public String url;
    public String document;
}