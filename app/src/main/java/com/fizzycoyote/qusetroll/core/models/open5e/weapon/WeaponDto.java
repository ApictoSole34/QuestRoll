package com.fizzycoyote.qusetroll.core.models.open5e.weapon;

import com.fizzycoyote.qusetroll.core.models.open5e.document.DocumentDto;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class WeaponDto implements Serializable {
    @SerializedName("key") public String key;
    @SerializedName("name") public String name;
    @SerializedName("damage_dice") public String damageDice;
    @SerializedName("damage_type") public DamageTypeDto damageType;
    @SerializedName("range") public float range;
    @SerializedName("long_range") public float longRange;
    @SerializedName("is_simple") public boolean isSimple;
    @SerializedName("is_improvised") public boolean isImprovised;
    @SerializedName("properties") public List<WeaponPropertyDto> properties;
    @SerializedName("document") public DocumentDto document;

    public static class DamageTypeDto implements Serializable {
        @SerializedName("name") public String name;
        @SerializedName("key") public String key;
    }

    public static class WeaponPropertyDto implements Serializable {
        @SerializedName("property") public PropertyDetail property;
        @SerializedName("detail") public String detail;

        public static class PropertyDetail implements Serializable {
            @SerializedName("name") public String name;
            @SerializedName("type") public String type;
            @SerializedName("desc") public String desc;
        }
    }
}