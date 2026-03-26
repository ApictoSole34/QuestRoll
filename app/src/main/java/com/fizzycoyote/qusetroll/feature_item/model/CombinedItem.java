package com.fizzycoyote.qusetroll.feature_item.model;

import com.fizzycoyote.qusetroll.core.models.custom.custom_item.CustomItemEntity;
import com.fizzycoyote.qusetroll.core.models.open5e.item.ItemDto;
import com.fizzycoyote.qusetroll.core.models.open5e.item.ItemEntity;
import com.google.gson.Gson;

import java.util.Objects;

public class CombinedItem {
    public final String id;
    public final String name;
    public final String categoryName;
    public final String categoryKey;
    public final String rarityName;
    public final boolean isMagicItem;
    public final String documentName;
    public final boolean isCustom;
    public final String key;
    public final long customId;

    public final String damageDice;
    public final String damageTypeName;
    public final float range;
    public final float longRange;
    public final boolean isSimple;
    public final boolean isImprovised;

    public final String acDisplay;
    public final int acBase;

    public CombinedItem(ItemEntity item) {
        this.id = item.key;
        this.key = item.key;
        this.customId = -1;
        this.name = item.name;
        this.categoryName = item.categoryName;
        this.categoryKey = item.categoryKey;
        this.rarityName = item.rarityName;
        this.isMagicItem = item.isMagicItem;
        this.documentName = item.documentName;
        this.isCustom = false;

        String tmpDamageDice = null;
        String tmpDamageTypeName = null;
        float tmpRange = 0f;
        float tmpLongRange = 0f;
        boolean tmpIsSimple = false;
        boolean tmpIsImprovised = false;
        if (item.weaponJson != null && !item.weaponJson.isEmpty()) {
            try {
                ItemDto.WeaponEmbedDto weapon = new Gson().fromJson(item.weaponJson, ItemDto.WeaponEmbedDto.class);
                tmpDamageDice = weapon.damageDice;
                tmpDamageTypeName = weapon.damageType != null ? weapon.damageType.name : null;
                tmpRange = weapon.range;
                tmpLongRange = weapon.longRange;
                tmpIsSimple = weapon.isSimple;
                tmpIsImprovised = weapon.isImprovised;
            } catch (Exception e) {}
        }
        this.damageDice = tmpDamageDice;
        this.damageTypeName = tmpDamageTypeName;
        this.range = tmpRange;
        this.longRange = tmpLongRange;
        this.isSimple = tmpIsSimple;
        this.isImprovised = tmpIsImprovised;

        String tmpAcDisplay = null;
        int tmpAcBase = 0;
        if (item.armorJson != null && !item.armorJson.isEmpty()) {
            try {
                ItemDto.ArmorEmbedDto armor = new Gson().fromJson(item.armorJson, ItemDto.ArmorEmbedDto.class);
                tmpAcDisplay = armor.acDisplay;
                tmpAcBase = armor.acBase;
            } catch (Exception e) {
            }
        }
        this.acDisplay = tmpAcDisplay;
        this.acBase = tmpAcBase;
    }

    public CombinedItem(CustomItemEntity item) {
        this.id = "custom_" + item.id;
        this.key = null;
        this.customId = item.id;
        this.name = item.name;
        this.categoryName = item.categoryName;
        this.categoryKey = item.categoryKey;
        this.rarityName = item.rarityName;
        this.isMagicItem = item.isMagicItem;
        this.documentName = "Custom";
        this.isCustom = true;

        String tmpDamageDice = null;
        String tmpDamageTypeName = null;
        float tmpRange = 0f;
        float tmpLongRange = 0f;
        boolean tmpIsSimple = false;
        boolean tmpIsImprovised = false;
        if (item.weaponJson != null && !item.weaponJson.isEmpty()) {
            try {
                ItemDto.WeaponEmbedDto weapon = new Gson().fromJson(item.weaponJson, ItemDto.WeaponEmbedDto.class);
                tmpDamageDice = weapon.damageDice;
                tmpDamageTypeName = weapon.damageType != null ? weapon.damageType.name : null;
                tmpRange = weapon.range;
                tmpLongRange = weapon.longRange;
                tmpIsSimple = weapon.isSimple;
                tmpIsImprovised = weapon.isImprovised;
            } catch (Exception e) {
            }
        }
        this.damageDice = tmpDamageDice;
        this.damageTypeName = tmpDamageTypeName;
        this.range = tmpRange;
        this.longRange = tmpLongRange;
        this.isSimple = tmpIsSimple;
        this.isImprovised = tmpIsImprovised;

        String tmpAcDisplay = null;
        int tmpAcBase = 0;
        if (item.armorJson != null && !item.armorJson.isEmpty()) {
            try {
                ItemDto.ArmorEmbedDto armor = new Gson().fromJson(item.armorJson, ItemDto.ArmorEmbedDto.class);
                tmpAcDisplay = armor.acDisplay;
                tmpAcBase = armor.acBase;
            } catch (Exception e) {
            }
        }
        this.acDisplay = tmpAcDisplay;
        this.acBase = tmpAcBase;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return Objects.equals(id, ((CombinedItem) o).id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }
}