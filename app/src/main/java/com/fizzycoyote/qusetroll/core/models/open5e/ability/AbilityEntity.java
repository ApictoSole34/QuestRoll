package com.fizzycoyote.qusetroll.core.models.open5e.ability;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "abilities")
public class AbilityEntity {
    @PrimaryKey @NonNull @ColumnInfo(name = "key") public String key;
    public String name;
    public String description;
    @ColumnInfo(name = "short_desc")
    public String shortDesc;
    @ColumnInfo(name = "document")
    public String documentUrl;
}
