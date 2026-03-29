package com.fizzycoyote.qusetroll.core.models.open5e.ability;

import com.fizzycoyote.qusetroll.core.models.open5e.ApiResponse;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class AbilityResponse {
    @SerializedName("count")   public int count;
    @SerializedName("next")    public String next;
    @SerializedName("results") public List<AbilityDto> results;
}