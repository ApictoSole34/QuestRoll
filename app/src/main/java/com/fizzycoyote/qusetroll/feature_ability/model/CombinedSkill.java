package com.fizzycoyote.qusetroll.feature_ability.model;

import com.fizzycoyote.qusetroll.core.models.custom.custom_ability.CustomSkillEntity;
import com.fizzycoyote.qusetroll.core.models.open5e.ability.skill.SkillEntity;

public class CombinedSkill {
    public final String id;
    public final String key;
    public final long   customId;
    public final String name;
    public final String abilityName;
    public final boolean isCustom;

    public CombinedSkill(SkillEntity e, String abilityName) {
        id = e.key; key = e.key; customId = -1;
        name = e.name; this.abilityName = abilityName; isCustom = false;
    }

    public CombinedSkill(CustomSkillEntity e) {
        id = "custom_" + e.id; key = null; customId = e.id;
        name = e.name; abilityName = e.abilityName; isCustom = true;
    }
}