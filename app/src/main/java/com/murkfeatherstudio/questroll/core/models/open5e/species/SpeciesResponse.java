package com.murkfeatherstudio.questroll.core.models.open5e.species;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class SpeciesResponse {
    @SerializedName("count") public int count;
    @SerializedName("next") public String next;
    @SerializedName("previous") public String previous;
    @SerializedName("results") public List<SpeciesDto> results;

    public List<SpeciesDto> getResults() { return results; }
}