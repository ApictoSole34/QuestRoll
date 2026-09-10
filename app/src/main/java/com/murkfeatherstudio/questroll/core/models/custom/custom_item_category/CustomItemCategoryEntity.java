package com.murkfeatherstudio.questroll.core.models.custom.custom_item_category;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "custom_item_categories")
public class CustomItemCategoryEntity {
    @PrimaryKey(autoGenerate = true)
    public long id;
    public String name;
    public String description;

    @ColumnInfo(name = "game_system")
    public String gameSystem = "5e-2014";
}
