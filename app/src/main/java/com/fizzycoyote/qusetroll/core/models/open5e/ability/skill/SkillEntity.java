package com.fizzycoyote.qusetroll.core.models.open5e.ability.skill;

import static androidx.room.ForeignKey.CASCADE;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.fizzycoyote.qusetroll.core.models.open5e.ability.AbilityEntity;

@Entity(
        tableName = "skills",
        foreignKeys = @ForeignKey(
                entity = AbilityEntity.class,
                parentColumns = "key",
                childColumns = "abilityKey",
                onDelete = CASCADE
        ),
        indices = @Index("abilityKey")
)
public class SkillEntity {
    @PrimaryKey @NonNull public String key;
    @NonNull public String abilityKey;
    public String name;
    public String description;
}