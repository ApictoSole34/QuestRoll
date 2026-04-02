package com.fizzycoyote.qusetroll.core.models.open5e.service;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ServiceResponse {
    @SerializedName("count") public int count;
    @SerializedName("next") public String next;
    @SerializedName("previous") public String previous;
    @SerializedName("results") public List<ServiceDto> results;
}