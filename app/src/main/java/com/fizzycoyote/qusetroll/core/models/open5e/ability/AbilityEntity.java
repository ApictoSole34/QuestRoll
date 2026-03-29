package com.fizzycoyote.qusetroll.core.models.open5e.ability;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "abilities")
public class AbilityEntity {
    @PrimaryKey
    @NonNull
    public String key;          // "cha", "dex", …

    public String name;         // "Charisma"
    public String shortDesc;    // "measuring force of personality"

    /** JSON: List<DescriptionDto> – stores per-gamesystem descriptions */
    public String descriptionsJson;
}