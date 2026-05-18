package com.fizzycoyote.qusetroll.core.models.character;

import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "character_traits",
        foreignKeys = @ForeignKey(entity = CharacterEntity.class,
                parentColumns = "id",
                childColumns = "character_id",
                onDelete = ForeignKey.CASCADE),
        indices = @Index("character_id"))
public class CharacterTraitEntity {
    @PrimaryKey(autoGenerate = true)
    public long id;

    @ColumnInfo(name = "character_id")
    public long characterId;

    @ColumnInfo(name = "source_type")
    public String sourceType;

    @Nullable
    @ColumnInfo(name = "source_key")
    public String sourceKey;

    public String name;
    public String description;

    @ColumnInfo(name = "level_requirement")
    public int levelRequirement;

    @ColumnInfo(name = "display_order")
    public int displayOrder;
}