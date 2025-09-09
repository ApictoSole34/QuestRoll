package com.fizzycoyote.qusetroll.core.models.open5e.damage_type;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class DamageTypeDto implements Serializable {
    @SerializedName("url") public String url;
    @SerializedName("key") public String key;
    @SerializedName("name") public String name;
    @SerializedName("desc") public String desc;
    @SerializedName("document") public String documentUrl;
}
