package com.murkfeatherstudio.questroll.core.models.character;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "character_resources",
        foreignKeys = @ForeignKey(entity = CharacterEntity.class,
                parentColumns = "id",
                childColumns = "character_id",
                onDelete = ForeignKey.CASCADE),
        indices = @Index("character_id"))
public class CharacterResourceEntity {
    @PrimaryKey(autoGenerate = true)
    public long id;

    @ColumnInfo(name = "character_id")
    public long characterId;

    public String name;        // e.g., "Rage", "Ki Points", "Superiority Dice"
    public int currentValue;
    public int maxValue;
    public String resetType;   // "SHORT_REST" or "LONG_REST"
}
