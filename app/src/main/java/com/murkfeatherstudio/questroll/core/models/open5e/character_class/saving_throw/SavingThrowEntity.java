package com.murkfeatherstudio.questroll.core.models.open5e.character_class.saving_throw;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;

import com.murkfeatherstudio.questroll.core.models.open5e.character_class.CharacterClassEntity;

@Entity(
        tableName = "saving_throws",
        primaryKeys = {"class_key_ref", "ability_key"},
        foreignKeys = @ForeignKey(
                entity = CharacterClassEntity.class,
                parentColumns = "class_key",
                childColumns = "class_key_ref",
                onDelete = ForeignKey.CASCADE
        )
)
public class SavingThrowEntity {
    @NonNull
    @ColumnInfo(name = "class_key_ref")
    public String classKey;

    @NonNull
    @ColumnInfo(name = "ability_key")
    public String abilityKey;
    public String abilityName;

    public SavingThrowEntity() {}
}