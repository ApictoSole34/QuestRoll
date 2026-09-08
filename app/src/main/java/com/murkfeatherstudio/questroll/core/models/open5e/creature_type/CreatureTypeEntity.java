package com.murkfeatherstudio.questroll.core.models.open5e.creature_type;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "creature_types")
public class CreatureTypeEntity {
    @PrimaryKey @NonNull public String key;
    public String name;
    public String description;
    @ColumnInfo(name = "document_name") public String documentName;
    @ColumnInfo(name = "document_key") public String documentKey;
}