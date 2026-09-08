package com.murkfeatherstudio.questroll.core.models.custom.custom_item_rarity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "custom_item_rarities")
public class CustomItemRarityEntity {
    @PrimaryKey(autoGenerate = true)
    public long id;

    public String key;
    public String name;
    public int rank;
    public String description;
}