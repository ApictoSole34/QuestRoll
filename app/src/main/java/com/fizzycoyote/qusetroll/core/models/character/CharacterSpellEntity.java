package com.fizzycoyote.qusetroll.core.models.character;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "character_spells",
        foreignKeys = @ForeignKey(entity = CharacterEntity.class,
                parentColumns = "id",
                childColumns = "character_id",
                onDelete = ForeignKey.CASCADE),
        indices = @Index("character_id"))
public class CharacterSpellEntity {
    @PrimaryKey(autoGenerate = true)
    public long id;

    @ColumnInfo(name = "character_id")
    public long characterId;

    @ColumnInfo(name = "spell_key")
    public String spellKey;

    @ColumnInfo(name = "is_prepared")
    public boolean isPrepared;
}