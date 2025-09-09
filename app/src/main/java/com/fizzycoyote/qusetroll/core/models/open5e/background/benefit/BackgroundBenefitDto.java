package com.fizzycoyote.qusetroll.core.models.open5e.background.benefit;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class BackgroundBenefitDto implements Serializable {
    @SerializedName("name") public String name;
    @SerializedName("desc") public String desc;
    @SerializedName("type") public String type;
}
