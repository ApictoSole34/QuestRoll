package com.murkfeatherstudio.questroll.core.models.open5e.feat;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class FeatResponse implements Serializable {
    @SerializedName("count") public int count;
    @SerializedName("next") public String next;
    @SerializedName("previous") public String previous;
    @SerializedName("results") public List<FeatDto> results;
}
