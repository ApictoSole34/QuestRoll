package com.murkfeatherstudio.questroll.core.models.custom.custom_item_rarity;

import androidx.room.ColumnInfo;
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

    @ColumnInfo(name = "game_system")
    public String gameSystem = "5e-2014";
}