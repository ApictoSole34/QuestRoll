package com.fizzycoyote.qusetroll.core.models.open5e.weapon;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class WeaponResponse {
    @SerializedName("count") public int count;
    @SerializedName("next") public String next;
    @SerializedName("results") public List<WeaponDto> results;
}
