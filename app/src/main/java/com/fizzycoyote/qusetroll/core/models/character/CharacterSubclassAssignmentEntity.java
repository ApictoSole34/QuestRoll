package com.fizzycoyote.qusetroll.core.models.character;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "character_subclasses")
public class CharacterSubclassAssignmentEntity {
    @PrimaryKey(autoGenerate = true)
    public long id;
    public long characterId;
    public String classKey;
    public String subclassKey;
}