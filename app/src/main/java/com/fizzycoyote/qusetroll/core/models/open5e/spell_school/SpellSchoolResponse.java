package com.fizzycoyote.qusetroll.core.models.open5e.spell_school;

import com.fizzycoyote.qusetroll.core.models.open5e.ApiResponse;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class SpellSchoolResponse {
    @SerializedName("count") public int count;
    @SerializedName("next") public String next;
    @SerializedName("previous") public String previous;
    @SerializedName("results") public List<SpellSchoolDto> results;

    public List<SpellSchoolDto> getResults() { return results; }
}