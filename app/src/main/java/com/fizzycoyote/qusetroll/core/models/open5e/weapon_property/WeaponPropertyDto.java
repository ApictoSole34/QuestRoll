package com.fizzycoyote.qusetroll.core.models.open5e.weapon_property;

import com.google.gson.annotations.SerializedName;

public class WeaponPropertyDto {
    @SerializedName("key") public String key;
    @SerializedName("name") public String name;
    @SerializedName("desc") public String desc;
    @SerializedName("document") public String document;
    @SerializedName("url") public String url;
    @SerializedName("type") public String type;
}