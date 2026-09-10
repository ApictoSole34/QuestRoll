package com.murkfeatherstudio.questroll.core.models.custom.custom_ability;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "custom_abilities")
public class CustomAbilityEntity {
    @PrimaryKey(autoGenerate = true)
    public long id;

    @NonNull
    public String name = "";

    public String shortDesc;
    public String description;

    @ColumnInfo(name = "game_system")
    public String gameSystem = "5e-2014";

    public long createdAt;
}