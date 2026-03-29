package com.fizzycoyote.qusetroll.feature_ability.model;

import com.fizzycoyote.qusetroll.core.models.custom.custom_ability.CustomAbilityEntity;
import com.fizzycoyote.qusetroll.core.models.open5e.ability.AbilityEntity;

public class CombinedAbility {
    public final String id;
    public final String key;
    public final long   customId;
    public final String name;
    public final String shortDesc;
    public final boolean isCustom;

    public CombinedAbility(AbilityEntity e) {
        id = e.key; key = e.key; customId = -1;
        name = e.name; shortDesc = e.shortDesc; isCustom = false;
    }

    public CombinedAbility(CustomAbilityEntity e) {
        id = "custom_" + e.id; key = null; customId = e.id;
        name = e.name; shortDesc = e.shortDesc; isCustom = true;
    }
}