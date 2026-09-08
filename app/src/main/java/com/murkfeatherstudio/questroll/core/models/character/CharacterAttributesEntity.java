package com.murkfeatherstudio.questroll.core.models.character;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "character_attributes",
        foreignKeys = @ForeignKey(entity = CharacterEntity.class,
                parentColumns = "id",
                childColumns = "character_id",
                onDelete = ForeignKey.CASCADE),
        indices = @Index("character_id"))
public class CharacterAttributesEntity {
    @PrimaryKey(autoGenerate = true)
    public long id;

    @ColumnInfo(name = "character_id")
    public long characterId;

    public int strength;
    public int dexterity;
    public int constitution;
    public int intelligence;
    public int wisdom;
    public int charisma;

    @ColumnInfo(name = "strength_mod")
    public int strengthMod;
    @ColumnInfo(name = "dexterity_mod")
    public int dexterityMod;
    @ColumnInfo(name = "constitution_mod")
    public int constitutionMod;
    @ColumnInfo(name = "intelligence_mod")
    public int intelligenceMod;
    @ColumnInfo(name = "wisdom_mod")
    public int wisdomMod;
    @ColumnInfo(name = "charisma_mod")
    public int charismaMod;
}