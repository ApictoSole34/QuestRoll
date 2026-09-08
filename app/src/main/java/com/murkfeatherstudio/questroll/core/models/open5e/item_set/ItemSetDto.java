package com.murkfeatherstudio.questroll.core.models.open5e.item_set;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ItemSetDto {
    @SerializedName("url") public String url;
    @SerializedName("key") public String key;
    @SerializedName("name") public String name;
    @SerializedName("desc") public String desc;
    @SerializedName("document") public String document;
    @SerializedName("items") public List<ItemRefDto> items;

    public static class ItemRefDto {
        @SerializedName("name") public String name;
        @SerializedName("key") public String key;
        @SerializedName("url") public String url;
    }
}
