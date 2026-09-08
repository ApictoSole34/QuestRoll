package com.murkfeatherstudio.questroll.core.models.campaign;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "campaigns")
public class CampaignEntity {
    @PrimaryKey(autoGenerate = true)
    public long id;

    public String name;
    public String description;

    @ColumnInfo(name = "game_system")
    public String gameSystem;

    @ColumnInfo(name = "character_id")
    public long characterId = -1;
}