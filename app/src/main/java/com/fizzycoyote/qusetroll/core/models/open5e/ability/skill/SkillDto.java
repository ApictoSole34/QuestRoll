package com.fizzycoyote.qusetroll.core.models.open5e.ability.skill;

import com.google.gson.annotations.SerializedName;

public class SkillDto {
    @SerializedName("key") public String key;
    @SerializedName("name") public String name;
    @SerializedName("desc") public String description;
}