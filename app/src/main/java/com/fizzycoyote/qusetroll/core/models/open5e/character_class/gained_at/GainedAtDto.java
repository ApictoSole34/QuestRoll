package com.fizzycoyote.qusetroll.core.models.open5e.character_class.gained_at;

import com.google.gson.annotations.SerializedName;

public class GainedAtDto {
    @SerializedName("level") public int level;
    @SerializedName("detail") public String detail;
}
