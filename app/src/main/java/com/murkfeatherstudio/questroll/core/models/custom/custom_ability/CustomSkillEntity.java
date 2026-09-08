package com.murkfeatherstudio.questroll.core.models.custom.custom_ability;

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

    public String abilityKey;
    public boolean parentIsCustom;

    public String abilityName;

    public long createdAt;
}