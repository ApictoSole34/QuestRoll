package com.fizzycoyote.qusetroll.feature_environment.model;

import com.fizzycoyote.qusetroll.core.models.custom.custom_environment.CustomEnvironmentEntity;
import com.fizzycoyote.qusetroll.core.models.open5e.environment.EnvironmentEntity;

import java.util.Objects;

public class CombinedEnvironment {
    public final String id;
    public final String name;
    public final String desc;
    public final boolean aquatic;
    public final boolean planar;
    public final boolean interior;
    public final boolean isCustom;
    public final String key;
    public final long customId;

    public CombinedEnvironment(EnvironmentEntity api) {
        this.id = api.key;
        this.key = api.key;
        this.customId = -1;
        this.name = api.name;
        this.desc = api.desc;
        this.aquatic = api.aquatic;
        this.planar = api.planar;
        this.interior = api.interior;
        this.isCustom = false;
    }

    public CombinedEnvironment(CustomEnvironmentEntity custom) {
        this.id = "custom_" + custom.id;
        this.key = null;
        this.customId = custom.id;
        this.name = custom.name;
        this.desc = custom.desc;
        this.aquatic = custom.aquatic;
        this.planar = custom.planar;
        this.interior = custom.interior;
        this.isCustom = true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return Objects.equals(id, ((CombinedEnvironment) o).id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
