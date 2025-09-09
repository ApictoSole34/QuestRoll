package com.fizzycoyote.qusetroll.core.models.open5e.ability;

import com.fizzycoyote.qusetroll.core.models.open5e.ability.skill.SkillDto;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class AbilityDto {
    @SerializedName("key") public String key;
    @SerializedName("name") public String name;
    @SerializedName("desc") public String description;
    @SerializedName("short_desc") public String shortDesc;
    @SerializedName("document") public String documentUrl;
    @SerializedName("skills") public List<SkillDto> skills;
}