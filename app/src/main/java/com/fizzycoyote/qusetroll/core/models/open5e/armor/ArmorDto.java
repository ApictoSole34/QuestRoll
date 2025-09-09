package com.fizzycoyote.qusetroll.core.models.open5e.armor;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class ArmorDto implements Serializable {
    @SerializedName("url") public String url;
    @SerializedName("key") public String key;
    @SerializedName("ac_display") public String acDisplay;
    @SerializedName("category") public String category;
    @SerializedName("name") public String name;
    @SerializedName("grants_stealth_disadvantage") public Boolean grantsStealthDisadvantage;
    @SerializedName("strength_requirement") public Integer strengthRequirement;
    @SerializedName("ac_base") public int acBase;
    @SerializedName("ac_add_dexxmod") public boolean acAddDexxmod;
    @SerializedName("ac_cap_dexmod") public Integer acCapDexmod;
    @SerializedName("document") public String document;
}
