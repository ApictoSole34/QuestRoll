package com.fizzycoyote.qusetroll.core.models.open5e.race.trait;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class TraitDto implements Serializable {
    @SerializedName("name") public String name;
    @SerializedName("desc") public String desc;
}
