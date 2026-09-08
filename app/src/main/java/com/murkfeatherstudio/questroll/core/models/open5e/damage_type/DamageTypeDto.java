package com.murkfeatherstudio.questroll.core.models.open5e.damage_type;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class DamageTypeDto implements Serializable {
    @SerializedName("key")
    public String key;
    @SerializedName("name")
    public String name;
    @SerializedName("url")
    public String url;
    @SerializedName("descriptions")
    public List<DescriptionDto> descriptions;
    @SerializedName("document")
    public String document;

    public static class DescriptionDto implements Serializable {
        @SerializedName("desc")
        public String desc;
        @SerializedName("document")
        public String document;
        @SerializedName("gamesystem")
        public String gamesystem;
    }
}