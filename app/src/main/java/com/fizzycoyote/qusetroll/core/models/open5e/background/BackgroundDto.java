package com.fizzycoyote.qusetroll.core.models.open5e.background;

import androidx.annotation.Nullable;

import com.fizzycoyote.qusetroll.core.models.open5e.background.benefit.BackgroundBenefitDto;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class BackgroundDto implements Serializable {
    @SerializedName("url") public String url;
    @SerializedName("key") public String key;
    @SerializedName("benefits") public List<BackgroundBenefitDto> benefits;
    @SerializedName("document") public String document;
    @SerializedName("name") public String name;
    @SerializedName("desc") @Nullable public String desc;
}
