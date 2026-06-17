package com.fizzycoyote.qusetroll.core.models.character;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Date;

@Entity(tableName = "characters")
public class CharacterEntity {
    @PrimaryKey(autoGenerate = true)
    public long id;

    @NonNull
    public String name;

    public int totalLevel;
    public int experience;

    @ColumnInfo(name = "alignment_key")
    public String alignmentKey;

    @ColumnInfo(name = "background_key")
    public String backgroundKey;

    @ColumnInfo(name = "species_key")
    public String speciesKey;

    @ColumnInfo(name = "game_system")
    public String gameSystem;

    @ColumnInfo(name = "current_hp")
    public int currentHp;

    @ColumnInfo(name = "current_gold")
    public float currentGold = 0f;

    @ColumnInfo(name = "max_hp")
    public int maxHp;

    @ColumnInfo(name = "temporary_hp")
    public int temporaryHp;

    @ColumnInfo(name = "exhaustion_level", defaultValue = "0")
    public int exhaustionLevel = 0;

    @ColumnInfo(name = "has_inspiration", defaultValue = "0")
    public boolean hasInspiration = false;

    public String imagePath;
    public String thumbnailPath;
}