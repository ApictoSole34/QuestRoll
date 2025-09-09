package com.fizzycoyote.qusetroll.core.models.open5e.weapon;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class WeaponDto implements Serializable {
    @SerializedName("url") public String url;
    @SerializedName("key") public String key;
    @SerializedName("name") public String name;
    @SerializedName("damage_dice") public String damageDice;
    @SerializedName("versatile_dice") public String versatileDice;
    @SerializedName("is_versatile") public boolean isVersatile;
    @SerializedName("is_martial") public boolean isMartial;
    @SerializedName("is_melee") public boolean isMelee;
    @SerializedName("ranged_attack_possible") public boolean rangedAttackPossible;
    @SerializedName("reach") public float reach;
    @SerializedName("range") public float range;
    @SerializedName("long_range") public float longRange;
    @SerializedName("is_finesse") public boolean isFinesse;
    @SerializedName("is_thrown") public boolean isThrown;
    @SerializedName("is_two_handed") public boolean isTwoHanded;
    @SerializedName("requires_ammunition") public boolean requiresAmmunition;
    @SerializedName("requires_loading") public boolean requiresLoading;
    @SerializedName("is_heavy") public boolean isHeavy;
    @SerializedName("is_light") public boolean isLight;
    @SerializedName("is_simple") public boolean isSimple;
    @SerializedName("is_improvised") public boolean isImprovised;
    @SerializedName("properties") public List<String> properties;
    @SerializedName("damage_type") public String damageTypeUrl;
    @SerializedName("document") public String documentUrl;
}
