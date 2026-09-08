package com.murkfeatherstudio.questroll.core.models.open5e.ability.skill;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.murkfeatherstudio.questroll.core.models.open5e.ability.AbilityEntity;

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
    public String key;

    public String name;
    public String abilityKey;
    public String documentKey;

    public String descriptionsJson;
}