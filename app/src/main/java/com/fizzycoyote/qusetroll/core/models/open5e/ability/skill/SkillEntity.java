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
                onDelete = ForeignKey.CASCADE
        ),
        indices = {@Index("abilityKey")}
)
public class SkillEntity {
    @PrimaryKey
    @NonNull
    public String key;          // "deception", "stealth", …

    public String name;         // "Deception"
    public String abilityKey;   // FK → AbilityEntity.key
    public String documentKey;  // "core", "a5e-ag", …

    /** JSON: List<DescriptionDto> */
    public String descriptionsJson;
}