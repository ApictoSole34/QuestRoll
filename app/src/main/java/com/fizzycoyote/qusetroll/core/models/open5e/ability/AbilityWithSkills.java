package com.fizzycoyote.qusetroll.core.models.open5e.ability;

import androidx.room.Embedded;
import androidx.room.Relation;

import com.fizzycoyote.qusetroll.core.models.open5e.ability.skill.SkillEntity;

import java.util.List;

public class  AbilityWithSkills {
    @Embedded
    public AbilityEntity ability;
    @Relation(
            parentColumn = "key",
            entityColumn = "abilityKey"
    )
    public List<SkillEntity> skills;
}
