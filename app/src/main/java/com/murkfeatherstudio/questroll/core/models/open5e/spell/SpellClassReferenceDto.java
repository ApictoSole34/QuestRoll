package com.murkfeatherstudio.questroll.core.models.open5e.spell;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class SpellClassReferenceDto implements Serializable {
    @SerializedName("name") public String name;
    @SerializedName("key") public String key;
    @SerializedName("url") public String url;
}
