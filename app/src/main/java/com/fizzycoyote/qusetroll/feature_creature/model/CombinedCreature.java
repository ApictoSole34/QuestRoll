package com.fizzycoyote.qusetroll.feature_creature.model;

import com.fizzycoyote.qusetroll.core.models.custom.custom_creature.CustomCreatureEntity;
import com.fizzycoyote.qusetroll.core.models.open5e.creature.CreatureEntity;

import java.util.Objects;

public class CombinedCreature {
    public final String id;
    public final String name;
    public final String crText;
    public final float crDecimal;
    public final String typeName;
    public final String sizeName;
    public final String alignment;
    public final boolean isCustom;
    public final String key;
    public final long customId;

    public CombinedCreature(CreatureEntity c) {
        this.id = c.key;
        this.key = c.key;
        this.customId = -1;
        this.name = c.name;
        this.crText = c.challengeRatingText;
        this.crDecimal = c.challengeRatingDecimal;
        this.typeName = c.typeName;
        this.sizeName = c.sizeName;
        this.alignment = c.alignment;
        this.isCustom = false;
    }

    public CombinedCreature(CustomCreatureEntity c) {
        this.id = "custom_" + c.id;
        this.key = null;
        this.customId = c.id;
        this.name = c.name;
        this.crText = c.crText;
        this.crDecimal = c.crDecimal;
        this.typeName = c.typeName;
        this.sizeName = c.sizeName;
        this.alignment = c.alignment;
        this.isCustom = true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return Objects.equals(id, ((CombinedCreature) o).id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }
}