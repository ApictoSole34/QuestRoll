package com.murkfeatherstudio.questroll.core.models.open5e.license;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class LicenseDto implements Serializable {
    @SerializedName("url") public String url;
    @SerializedName("key") public String key;
    @SerializedName("name") public String name;
    @SerializedName("desc") public String desc;
}
