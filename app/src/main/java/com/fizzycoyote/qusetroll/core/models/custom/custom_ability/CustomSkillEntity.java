package com.fizzycoyote.qusetroll.core.models.custom.custom_ability;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "custom_skills")
public class CustomSkillEntity {
    @PrimaryKey(autoGenerate = true)
    public long id;

    @NonNull
    public String name = "";

    public String description;

    /**
     * Key of the parent ability.
     * - If parentIsCustom == false → matches AbilityEntity.key ("cha", "dex", …)
     * - If parentIsCustom == true  → String.valueOf(CustomAbilityEntity.id)
     */
    public String abilityKey;
    public boolean parentIsCustom;

    /** Human-readable ability name, denormalized for easy display */
    public String abilityName;

    public long createdAt;
}