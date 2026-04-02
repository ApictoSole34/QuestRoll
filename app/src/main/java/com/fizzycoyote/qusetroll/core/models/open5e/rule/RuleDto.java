package com.fizzycoyote.qusetroll.core.models.open5e.rule;

import com.google.gson.annotations.SerializedName;

public class RuleDto {
    @SerializedName("url") public String url;
    @SerializedName("name") public String name;
    @SerializedName("desc") public String desc;
    @SerializedName("index") public int index;
    @SerializedName("initialHeaderLevel") public int initialHeaderLevel;
    @SerializedName("document") public String document;
    @SerializedName("ruleset") public String ruleset;
}