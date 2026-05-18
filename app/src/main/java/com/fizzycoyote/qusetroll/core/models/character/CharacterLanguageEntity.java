package com.fizzycoyote.qusetroll.core.models.character;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "character_languages",
        foreignKeys = @ForeignKey(entity = CharacterEntity.class,
                parentColumns = "id",
                childColumns = "character_id",
                onDelete = ForeignKey.CASCADE),
        indices = {@Index("character_id")})
public class CharacterLanguageEntity {
    @PrimaryKey(autoGenerate = true)
    public long id;

    @ColumnInfo(name = "character_id")
    public long characterId;

    public String languageKey;
    public String languageName;
    public boolean isSecret;
}