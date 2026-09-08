package com.murkfeatherstudio.questroll.feature_spell.spell_school.model;

import com.murkfeatherstudio.questroll.core.models.custom.custom_spell.CustomSpellSchoolEntity;
import com.murkfeatherstudio.questroll.core.models.open5e.spell_school.SpellSchoolEntity;

import java.util.Objects;

public class CombinedSpellSchool {
    public final String id;
    public final String name;
    public final String description;
    public final boolean isCustom;
    public final String key;
    public final long customId;

    public CombinedSpellSchool(SpellSchoolEntity api) {
        this.id = api.key;
        this.key = api.key;
        this.customId = -1;
        this.name = api.name;
        this.description = api.desc;
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