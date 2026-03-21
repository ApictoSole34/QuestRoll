package com.fizzycoyote.qusetroll.core.models.open5e.creature;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class CreatureResponse {
    @SerializedName("count") public int count;
    @SerializedName("next") public String next;
    @SerializedName("results") public List<CreatureDto> results;
}
