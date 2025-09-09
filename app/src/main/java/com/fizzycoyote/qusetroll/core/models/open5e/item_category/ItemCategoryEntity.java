package com.fizzycoyote.qusetroll.core.models.open5e.item_category;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "item_categories")
public class ItemCategoryEntity {
    @PrimaryKey @NonNull public String key;
    public String url;
    public String name;
    public String document;
}
