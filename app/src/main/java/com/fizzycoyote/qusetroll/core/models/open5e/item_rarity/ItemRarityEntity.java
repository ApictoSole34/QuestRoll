package com.fizzycoyote.qusetroll.core.models.open5e.item_rarity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "item_rarities")
public class ItemRarityEntity {
    @PrimaryKey
    @NonNull
    public String key;
    public String name;
    public String url;
    public int rank;
}