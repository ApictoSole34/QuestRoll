package com.fizzycoyote.qusetroll.feature_condition.model;

import com.fizzycoyote.qusetroll.core.models.custom.custom_condition.CustomConditionEntity;
import com.fizzycoyote.qusetroll.core.models.open5e.condition.ConditionEntity;

import java.util.Objects;

public class CombinedCondition {
    public final String id;
    public final String name;
    public final String description;
    public final boolean isCustom;
    public final String key;
    public final long customId;

    public CombinedCondition(ConditionEntity api) {
        this.id = api.key;
        this.key = api.key;
        this.customId = -1;
        this.name = api.name;
        this.description = api.description;
        this.isCustom = false;
    }

    public CombinedCondition(CustomConditionEntity custom) {
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
        return Objects.equals(id, ((CombinedCondition) o).id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}