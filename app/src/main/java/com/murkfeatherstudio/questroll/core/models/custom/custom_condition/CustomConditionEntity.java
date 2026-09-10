package com.murkfeatherstudio.questroll.core.models.custom.custom_condition;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "custom_conditions")
public class CustomConditionEntity {
    @PrimaryKey(autoGenerate = true)
    public long id;
    public String name;
    public String description;

    @ColumnInfo(name = "game_system")
    public String gameSystem = "5e-2014";
}
