package com.murkfeatherstudio.questroll.core.models.custom.custom_character_class.custom_class_progression;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "custom_class_progression")
public class CustomClassProgressionEntity {
    @PrimaryKey(autoGenerate = true)
    public long id;

    @ColumnInfo(name = "class_key")
    public String classKey;

    public int level;

    @ColumnInfo(name = "proficiency_bonus")
    public int proficiencyBonus;

    @ColumnInfo(name = "progression_data")
    public String progressionData;

    @ColumnInfo(name = "game_system")
    public String gameSystem = "5e-2014";
}