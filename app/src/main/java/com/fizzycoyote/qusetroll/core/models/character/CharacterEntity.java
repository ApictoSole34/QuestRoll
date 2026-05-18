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

    public int currentHp;
    public int temporaryHp;

    public String imagePath;
    public String thumbnailPath;
}