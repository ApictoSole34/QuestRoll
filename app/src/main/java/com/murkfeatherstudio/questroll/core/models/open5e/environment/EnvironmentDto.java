package com.murkfeatherstudio.questroll.core.models.open5e.environment;

import com.google.gson.annotations.SerializedName;

public class EnvironmentDto {
    @SerializedName("key") public String key;
    @SerializedName("name") public String name;
    @SerializedName("desc") public String desc;
    @SerializedName("aquatic") public boolean aquatic;
    @SerializedName("planar") public boolean planar;
    @SerializedName("interior") public boolean interior;
    @SerializedName("url") public String url;
    @SerializedName("document") public String document;
}