package com.fizzycoyote.qusetroll.core.models.custom.custom_background;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "custom_backgrounds")
public class CustomBackgroundEntity {

    @PrimaryKey(autoGenerate = true)
    public long id;

    @NonNull
    public String key;
    public String name;
    public String desc;
    public String gameSystem;

    @ColumnInfo(name = "equipment_json")
    public String equipmentJson;

    @ColumnInfo(name = "languages_json")
    public String languagesJson;

    @ColumnInfo(name = "skill_proficiencies_json")
    public String skillProficienciesJson;

    @ColumnInfo(name = "tool_proficiencies_json")
    public String toolProficienciesJson;

    @ColumnInfo(name = "starting_gold")
    public int startingGold;

    @ColumnInfo(name = "features_json")
    public String featuresJson;
}