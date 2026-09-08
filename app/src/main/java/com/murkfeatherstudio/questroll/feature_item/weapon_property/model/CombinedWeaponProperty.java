package com.murkfeatherstudio.questroll.feature_item.weapon_property.model;

import com.murkfeatherstudio.questroll.core.models.custom.custom_weapon_property.CustomWeaponPropertyEntity;
import com.murkfeatherstudio.questroll.core.models.open5e.weapon_property.WeaponPropertyEntity;

import java.util.Objects;

public class CombinedWeaponProperty {
    public final String id;
    public final String name;
    public final String desc;
    public final String type;
    public final boolean isCustom;
    public final String key;
    public final long customId;

    public CombinedWeaponProperty(WeaponPropertyEntity api) {
        this.id = api.key;
        this.key = api.key;
        this.customId = -1;
        this.name = api.name;
        this.desc = api.desc;
        this.type = api.type != null ? api.type : "Property";
        this.isCustom = false;
    }

    public CombinedWeaponProperty(CustomWeaponPropertyEntity custom) {
        this.id = "custom_" + custom.id;
        this.key = null;
        this.customId = custom.id;
        this.name = custom.name;
        this.desc = custom.desc;
        this.type = custom.type != null ? custom.type : "Property";
        this.isCustom = true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return Objects.equals(id, ((CombinedWeaponProperty) o).id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}