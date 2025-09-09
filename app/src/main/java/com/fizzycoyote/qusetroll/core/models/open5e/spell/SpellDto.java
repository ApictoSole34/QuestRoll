package com.fizzycoyote.qusetroll.core.models.open5e.spell;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class SpellDto implements Serializable {
    @SerializedName("url") public String url;
    @SerializedName("document") public String document;
    @SerializedName("key") public String key;
    @SerializedName("casting_options") public List<CastingOptionDto> castingOptions;
    @SerializedName("school") public String school;
    @SerializedName("classes") public List<String> classes;
    @SerializedName("range_unit") public String rangeUnit;
    @SerializedName("shape_size_unit") public String shapeSizeUnit;
    @SerializedName("name") public String name;
    @SerializedName("desc") public String desc;
    @SerializedName("level") public int level;
    @SerializedName("higher_level") public String higherLevel;
    @SerializedName("target_type") public String targetType;
    @SerializedName("range_text") public String rangeText;
    @SerializedName("range") public float range;
    @SerializedName("ritual") public boolean ritual;
    @SerializedName("casting_time") public String castingTime;
    @SerializedName("reaction_condition") public String reactionCondition;
    @SerializedName("verbal") public boolean verbal;
    @SerializedName("somatic") public boolean somatic;
    @SerializedName("material") public boolean material;
    @SerializedName("material_specified") public String materialSpecified;
    @SerializedName("material_cost") public String materialCost;
    @SerializedName("material_consumed") public boolean materialConsumed;
    @SerializedName("target_count") public int targetCount;
    @SerializedName("saving_throw_ability") public String savingThrowAbility;
    @SerializedName("attack_roll") public boolean attackRoll;
    @SerializedName("damage_roll") public String damageRoll;
    @SerializedName("damage_types") public List<String> damageTypes;
    @SerializedName("duration") public String duration;
    @SerializedName("shape_type") public String shapeType;
    @SerializedName("shape_size") public Float shapeSize;
    @SerializedName("concentration") public boolean concentration;
}
