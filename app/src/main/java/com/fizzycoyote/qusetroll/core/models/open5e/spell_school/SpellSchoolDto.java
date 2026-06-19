package com.fizzycoyote.qusetroll.core.models.open5e.spell_school;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class SpellSchoolDto implements Serializable {
    @SerializedName("key")
    public String key;

    @SerializedName("name")
    public String name;

    @SerializedName("desc")
    public String desc;

    @SerializedName("document")
    public String document;
}