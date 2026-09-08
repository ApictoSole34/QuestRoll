package com.murkfeatherstudio.questroll.core.models.custom.custom_item_set;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.murkfeatherstudio.questroll.core.models.open5e.Converters;

import java.util.List;

@Entity(tableName = "custom_item_sets")
public class CustomItemSetEntity {
    @PrimaryKey(autoGenerate = true)
    public long id;

    public String name;
    public String desc;

    @TypeConverters(Converters.class)
    @ColumnInfo(name = "item_keys")
    public List<String> itemKeys;
}