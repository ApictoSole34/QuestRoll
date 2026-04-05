package com.fizzycoyote.qusetroll.core.models.custom.custom_item_category;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "custom_item_categories")
public class CustomItemCategoryEntity {
    @PrimaryKey(autoGenerate = true)
    public long id;
    public String name;
    public String description;
}
