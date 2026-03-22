package com.fizzycoyote.qusetroll.feature_item.model.weapon;

import com.fizzycoyote.qusetroll.core.models.custom.custom_weapon.CustomWeaponEntity;
import com.fizzycoyote.qusetroll.core.models.open5e.weapon.WeaponEntity;

import java.util.Objects;

public class CombinedWeapon {
    public final String id;
    public final String name;
    public final String damageDice;
    public final String damageTypeName;
    public final float range;
    public final float longRange;
    public final boolean isSimple;
    public final String documentName;
    public final boolean isCustom;
    public final String key;
    public final long customId;

    public CombinedWeapon(WeaponEntity w) {
        this.id = w.key;
        this.key = w.key;
        this.customId = -1;
        this.name = w.name;
        this.damageDice = w.damageDice;
        this.damageTypeName = w.damageTypeName;
        this.range = w.range;
        this.longRange = w.longRange;
        this.isSimple = w.isSimple;
        this.documentName = w.documentName;
        this.isCustom = false;
    }

    public CombinedWeapon(CustomWeaponEntity w) {
        this.id = "custom_" + w.id;
        this.key = null;
        this.customId = w.id;
        this.name = w.name;
        this.damageDice = w.damageDice;
        this.damageTypeName = w.damageTypeName;
        this.range = w.range;
        this.longRange = w.longRange;
        this.isSimple = w.isSimple;
        this.documentName = "Custom";
        this.isCustom = true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return Objects.equals(id, ((CombinedWeapon) o).id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }
}
