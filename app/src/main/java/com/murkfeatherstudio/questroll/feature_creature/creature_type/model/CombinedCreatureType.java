package com.murkfeatherstudio.questroll.feature_creature.creature_type.model;

import com.murkfeatherstudio.questroll.core.models.custom.custom_creature_type.CustomCreatureTypeEntity;
import com.murkfeatherstudio.questroll.core.models.open5e.creature_type.CreatureTypeEntity;

import java.util.Objects;

public class CombinedCreatureType {
    public final String id;
    public final String name;
    public final String description;
    public final boolean isCustom;
    public final String key;
    public final long customId;

    public CombinedCreatureType(CreatureTypeEntity api) {
        this.id = api.key;
        this.key = api.key;
        this.customId = -1;
        this.name = api.name;
        this.description = api.description;
        this.isCustom = false;
    }

    public CombinedCreatureType(CustomCreatureTypeEntity custom) {
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
        return Objects.equals(id, ((CombinedCreatureType) o).id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
