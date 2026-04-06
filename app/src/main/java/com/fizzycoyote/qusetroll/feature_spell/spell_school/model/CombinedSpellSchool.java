package com.fizzycoyote.qusetroll.feature_spell.spell_school.model;

import com.fizzycoyote.qusetroll.core.models.custom.custom_spell.CustomSpellSchoolEntity;
import com.fizzycoyote.qusetroll.core.models.open5e.spell_school.SpellSchoolEntity;

import java.util.Objects;

public class CombinedSpellSchool {
    public final String id;
    public final String name;
    public final String description;
    public final boolean isCustom;
    public final String key;
    public final long customId;

    public CombinedSpellSchool(SpellSchoolEntity api) {
        this.id = api.slug;
        this.key = api.slug;
        this.customId = -1;
        this.name = api.name;
        this.description = api.description;
        this.isCustom = false;
    }

    public CombinedSpellSchool(CustomSpellSchoolEntity custom) {
        this.id = "custom_" + custom.id;
        this.key = null;
        this.customId = custom.id;
        this.name = custom.name;
        this.description = custom.description;
        this.isCustom = true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return Objects.equals(id, ((CombinedSpellSchool) o).id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}