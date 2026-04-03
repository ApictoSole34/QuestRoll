package com.fizzycoyote.qusetroll.core.models.open5e.condition;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class ConditionResponse {
    @SerializedName("count") public int count;
    @SerializedName("next") public String next;
    @SerializedName("previous") public String previous;
    @SerializedName("results") public List<ConditionDto> results;
}