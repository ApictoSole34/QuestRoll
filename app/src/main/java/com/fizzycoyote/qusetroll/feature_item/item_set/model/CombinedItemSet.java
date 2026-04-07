package com.fizzycoyote.qusetroll.feature_item.item_set.model;

import com.fizzycoyote.qusetroll.core.models.custom.custom_item_set.CustomItemSetEntity;
import com.fizzycoyote.qusetroll.core.models.open5e.item_set.ItemSetEntity;

import java.util.List;
import java.util.Objects;

public class CombinedItemSet {
    public final String id;
    public final String name;
    public final String desc;
    public final List<String> itemKeys;
    public final boolean isCustom;
    public final String key;
    public final long customId;

    public CombinedItemSet(ItemSetEntity api) {
        this.id = api.key;
        this.key = api.key;
        this.customId = -1;
        this.name = api.name;
        this.desc = api.desc;
        this.itemKeys = api.itemKeys;
        this.isCustom = false;
    }

    public CombinedItemSet(CustomItemSetEntity custom) {
        this.id = "custom_" + custom.id;
        this.key = null;
        this.customId = custom.id;
        this.name = custom.name;
        this.desc = custom.desc;
        this.itemKeys = custom.itemKeys;
        this.isCustom = true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return Objects.equals(id, ((CombinedItemSet) o).id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}