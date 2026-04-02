package com.fizzycoyote.qusetroll.feature_service.model;

import com.fizzycoyote.qusetroll.core.models.custom.custom_service.CustomServiceEntity;
import com.fizzycoyote.qusetroll.core.models.open5e.service.ServiceEntity;

import java.util.Objects;

public class CombinedService {
    public final String id;
    public final String name;
    public final String desc;
    public final String cost;
    public final String detail;
    public final boolean isCustom;
    public final String key;
    public final long customId;

    public CombinedService(ServiceEntity api) {
        this.id = api.key;
        this.key = api.key;
        this.customId = -1;
        this.name = api.name;
        this.desc = api.desc;
        this.cost = api.cost;
        this.detail = api.detail;
        this.isCustom = false;
    }

    public CombinedService(CustomServiceEntity custom) {
        this.id = "custom_" + custom.id;
        this.key = null;
        this.customId = custom.id;
        this.name = custom.name;
        this.desc = custom.desc;
        this.cost = custom.cost;
        this.detail = custom.detail;
        this.isCustom = true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return Objects.equals(id, ((CombinedService) o).id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
