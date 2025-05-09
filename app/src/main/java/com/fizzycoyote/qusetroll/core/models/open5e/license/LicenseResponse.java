package com.fizzycoyote.qusetroll.core.models.open5e.license;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class LicenseResponse implements Serializable {
    @SerializedName("count") public int count;
    @SerializedName("next") public String next;
    @SerializedName("previous") public String previous;
    @SerializedName("results") public List<LicenseDto> results;
}
