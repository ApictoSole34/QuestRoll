package com.fizzycoyote.qusetroll.core.models.open5e.item_set;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.fizzycoyote.qusetroll.core.models.open5e.Converters;

import java.util.List;

@Entity(tableName = "item_sets")
public class ItemSetEntity {
    @PrimaryKey @NonNull public String key;
    public String name;
    public String desc;
    @ColumnInfo(name = "document_url") public String documentUrl;
    @TypeConverters(Converters.class)
    @ColumnInfo(name = "item_keys") public List<String> itemKeys;
}