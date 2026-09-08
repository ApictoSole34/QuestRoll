package com.murkfeatherstudio.questroll.core.models.open5e.spell;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class SchoolDto implements Serializable {
    @SerializedName("key") public String key;
    @SerializedName("name") public String name;
    @SerializedName("url") public String url;
}