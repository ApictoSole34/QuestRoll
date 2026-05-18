package com.fizzycoyote.qusetroll.core.models.character;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "character_classes",
        foreignKeys = @ForeignKey(entity = CharacterEntity.class,
                parentColumns = "id",
                childColumns = "character_id",
                onDelete = ForeignKey.CASCADE),
        indices = @Index("character_id"))
public class CharacterClassAssignmentEntity {
    @PrimaryKey(autoGenerate = true)
    public long id;

    @ColumnInfo(name = "character_id")
    public long characterId;

    @ColumnInfo(name = "class_key")
    public String classKey;

    public int level;
}