package com.fizzycoyote.qusetroll.core.models.open5e.item;

import androidx.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class ItemDto implements Serializable {
    @SerializedName("url") public String url;
    @SerializedName("key") public String key;
    @SerializedName("is_magic_item") public boolean isMagicItem;
    @SerializedName("weapon") @Nullable public String weaponUrl;
    @SerializedName("armor") @Nullable public String armorUrl;
    @SerializedName("document") public String documentUrl;
    @SerializedName("category") public String category;
    @SerializedName("rarity") @Nullable public String rarity;
    @SerializedName("name") public String name;
    @SerializedName("desc") public String desc;
    @SerializedName("weight") public String weight;
    @SerializedName("armor_class") @Nullable public int armorClass;
    @SerializedName("hit_points") @Nullable public int hitPoints;
    @SerializedName("hit_dice") @Nullable public String hitDice;
    @SerializedName("nonmagical_attack_resistance") @Nullable public boolean nonmagicalAttackResistance;
    @SerializedName("nonmagical_attack_immunity") @Nullable public boolean nonmagicalAttackImmunity;
    @SerializedName("cost") @Nullable public String cost;
    @SerializedName("requaiers_attunement") public boolean requaiersAttunement;
    @SerializedName("size") public String size;
    @SerializedName("damage_vulnerabilites") @Nullable public List<String> damageVulnerabilites;
    @SerializedName("damage_immunities") @Nullable public List<String> damageImmunities;
    @SerializedName("damage_resistances") @Nullable public List<String> damageResistances;
}
