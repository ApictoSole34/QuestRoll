package com.murkfeatherstudio.questroll.core.models.open5e.license;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "licenses")
public class LicenseEntity {
    @PrimaryKey @NonNull public String key;
    public String url;
    public String name;
    public String desc;
}
