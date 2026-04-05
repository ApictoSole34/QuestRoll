package com.fizzycoyote.qusetroll.feature_item.item_category.model;

import com.fizzycoyote.qusetroll.core.models.custom.custom_item_category.CustomItemCategoryEntity;
import com.fizzycoyote.qusetroll.core.models.open5e.item_category.ItemCategoryEntity;
import java.util.Objects;

public class CombinedItemCategory {
    public final String id;
    public final String name;
    public final String description;
    public final boolean isCustom;
    public final String key;
    public final long customId;

    public CombinedItemCategory(ItemCategoryEntity api) {
        this.id = api.key;
        this.key = api.key;
        this.customId = -1;
        this.name = api.name;
        this.description = null;
        this.isCustom = false;
    }

    public CombinedItemCategory(CustomItemCategoryEntity custom) {
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
        return Objects.equals(id, ((CombinedItemCategory) o).id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}