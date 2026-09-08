package com.murkfeatherstudio.questroll.core.models.open5e.item_category;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "item_categories")
public class ItemCategoryEntity {
    @PrimaryKey @NonNull public String key;
    public String name;
    @ColumnInfo(name = "document_name") public String documentName;
    @ColumnInfo(name = "document_key") public String documentKey;
}