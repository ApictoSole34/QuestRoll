package com.fizzycoyote.qusetroll.core.models.open5e.environment;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class EnvironmentResponse {
    @SerializedName("count") public int count;
    @SerializedName("next") public String next;
    @SerializedName("previous") public String previous;
    @SerializedName("results") public List<EnvironmentDto> results;
}