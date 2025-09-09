package com.fizzycoyote.qusetroll.core.models.open5e.feat;

import androidx.annotation.Nullable;

import com.fizzycoyote.qusetroll.core.models.open5e.feat.benefit.FeatBenefitDto;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class FeatDto implements Serializable {
    @SerializedName("url") public String url;
    @SerializedName("key") public String key;
    @SerializedName("has_prerequisite") public boolean hasPrerequisite;
    @SerializedName("benefits") public List<FeatBenefitDto> benefits;
    @SerializedName("document") public String document;
    @SerializedName("name") public String name;
    @SerializedName("desc") public String desc;
    @SerializedName("prerequisites") @Nullable public String prerequisites;
}
