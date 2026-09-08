package com.murkfeatherstudio.questroll.core.models.open5e.item_set;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ItemSetResponse {
    @SerializedName("count") public int count;
    @SerializedName("next") public String next;
    @SerializedName("previous") public String previous;
    @SerializedName("results") public List<ItemSetDto> results;
}