package com.fizzycoyote.qusetroll.core.models.open5e.spell;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class SpellResponse {
    @SerializedName("count") public int count;
    @SerializedName("next") public String next;
    @SerializedName("previous") public String previous;
    @SerializedName("results") public List<SpellDto> results;

    public List<SpellDto> getResults() { return results; }
}


