package com.murkfeatherstudio.questroll.core.models.open5e.item;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ItemResponse {
    @SerializedName("count") public int count;
    @SerializedName("next") public String next;
    @SerializedName("results") public List<ItemDto> results;
}