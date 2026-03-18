package com.fizzycoyote.qusetroll.feature_spell.model;

import com.fizzycoyote.qusetroll.core.models.custom.custom_spell.CustomSpellEntity;
import com.fizzycoyote.qusetroll.core.models.open5e.spell.SpellEntity;

import java.util.Objects;

public class CombinedSpell {
    public final String id;
    public final String name;
    public final int level;
    public final String schoolName;
    public final String castingTime;
    public final boolean ritual;
    public final boolean concentration;
    public final boolean isCustom;

    public final String key;
    public final long customId;

    public CombinedSpell(SpellEntity spell) {
        this.id = spell.key;
        this.key = spell.key;
        this.customId = -1;
        this.name = spell.name;
        this.level = spell.level;
        this.schoolName = spell.schoolName;
        this.castingTime = spell.castingTime;
        this.ritual = spell.ritual;
        this.concentration = spell.concentration;
        this.isCustom = false;
    }

    public CombinedSpell(CustomSpellEntity spell) {
        this.id = "custom_" + spell.id;
        this.key = null;
        this.customId = spell.id;
        this.name = spell.name;
        this.level = spell.level;
        this.schoolName = spell.schoolName;
        this.castingTime = spell.castingTime;
        this.ritual = spell.ritual;
        this.concentration = spell.concentration;
        this.isCustom = true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CombinedSpell that = (CombinedSpell) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}