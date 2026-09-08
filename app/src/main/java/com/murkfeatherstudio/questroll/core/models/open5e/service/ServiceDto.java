package com.murkfeatherstudio.questroll.core.models.open5e.service;

import com.murkfeatherstudio.questroll.core.models.open5e.document.DocumentDto;
import com.google.gson.annotations.SerializedName;

public class ServiceDto {
    @SerializedName("key") public String key;
    @SerializedName("name") public String name;
    @SerializedName("desc") public String desc;
    @SerializedName("cost") public String cost;
    @SerializedName("detail") public String detail;
    @SerializedName("url") public String url;
    @SerializedName("document") public DocumentDto document;
}