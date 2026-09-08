package com.murkfeatherstudio.questroll.core.models.open5e.character_class.hit_points;

import com.google.gson.annotations.SerializedName;

public class HitPointsDto {
    @SerializedName("hit_dice")
    public String hitDice;

    @SerializedName("hit_dice_name")
    public String hitDiceName;

    @SerializedName("hit_points_at_1st_level")
    public String at1stLevel;

    @SerializedName("hit_points_at_higher_levels")
    public String atHigherLevels;
}