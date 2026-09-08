package com.murkfeatherstudio.questroll.core.models.open5e.weapon_property;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class WeaponPropertyResponse {
    @SerializedName("count") public int count;
    @SerializedName("next") public String next;
    @SerializedName("previous") public String previous;
    @SerializedName("results") public List<WeaponPropertyDto> results;
}