package com.murkfeatherstudio.questroll.core.models.open5e.item_rarity;

import com.google.gson.annotations.SerializedName;

public class ItemRarityDto {
    @SerializedName("name") public String name;
    @SerializedName("key") public String key;
    @SerializedName("url") public String url;
    @SerializedName("rank") public int rank;
}