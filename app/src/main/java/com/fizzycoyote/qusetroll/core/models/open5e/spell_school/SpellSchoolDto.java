package com.fizzycoyote.qusetroll.core.models.open5e.spell_school;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class SpellSchoolDto implements Serializable {
    @SerializedName("url") public String url;
    @SerializedName("name") public String name;
    @SerializedName("desc") public String description;
    @SerializedName("document") public String documentUrl;
}
