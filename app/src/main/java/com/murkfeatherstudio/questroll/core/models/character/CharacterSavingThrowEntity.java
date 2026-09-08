package com.murkfeatherstudio.questroll.core.models.character;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "character_saving_throws",
        foreignKeys = @ForeignKey(entity = CharacterEntity.class,
                parentColumns = "id",
                childColumns = "character_id",
                onDelete = ForeignKey.CASCADE),
        indices = @Index("character_id"))
public class CharacterSavingThrowEntity {
    @PrimaryKey(autoGenerate = true)
    public long id;

    @ColumnInfo(name = "character_id")
    public long characterId;

    @ColumnInfo(name = "ability_key") // "STR", "DEX", "CON", "INT", "WIS", "CHA"
    public String abilityKey;

    @ColumnInfo(name = "is_proficient")
    public boolean isProficient;
}