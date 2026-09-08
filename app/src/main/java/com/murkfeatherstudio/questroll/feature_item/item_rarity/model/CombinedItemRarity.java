package com.murkfeatherstudio.questroll.feature_item.item_rarity.model;

import com.murkfeatherstudio.questroll.core.models.custom.custom_item_rarity.CustomItemRarityEntity;
import com.murkfeatherstudio.questroll.core.models.open5e.item_rarity.ItemRarityEntity;

import java.util.Objects;

public class CombinedItemRarity {
    public final String id;
    public final String name;
    public final int rank;
    public final String description;
    public final boolean isCustom;
    public final String key;
    public final long customId;

    public CombinedItemRarity(ItemRarityEntity api) {
        this.id = api.key;
        this.key = api.key;
        this.customId = -1;
        this.name = api.name;
        this.rank = api.rank;
        this.description = null;
        this.isCustom = false;
    }

    public CombinedItemRarity(CustomItemRarityEntity custom) {
        this.id = "custom_" + custom.id;
        this.key = null;
        this.customId = custom.id;
        this.name = custom.name;
        this.rank = custom.rank;
        this.description = custom.description;
        this.isCustom = true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return Objects.equals(id, ((CombinedItemRarity) o).id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
