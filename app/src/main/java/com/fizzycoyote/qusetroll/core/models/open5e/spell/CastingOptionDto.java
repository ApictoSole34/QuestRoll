package com.fizzycoyote.qusetroll.core.models.open5e.spell;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class CastingOptionDto implements Serializable {
    @SerializedName("type") public String type;
    @SerializedName("damage_roll") public String damageRoll;
    @SerializedName("target_count") public Integer targetCount;
    @SerializedName("duration") public String duration;
    @SerializedName("range") public float range;
    @SerializedName("concentration") public Boolean concentration;
    @SerializedName("shape_size") public float shapeSize;

}
