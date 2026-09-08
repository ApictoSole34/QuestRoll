package com.murkfeatherstudio.questroll.core.models.open5e.rule_set;

import com.murkfeatherstudio.questroll.core.models.open5e.document.DocumentDto;
import com.murkfeatherstudio.questroll.core.models.open5e.rule.RuleDto;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class RulesetDto {

    @SerializedName("key")
    public String key;

    @SerializedName("name")
    public String name;

    @SerializedName("desc")
    public String desc;

    @SerializedName("document")
    public DocumentDto document;

    @SerializedName("rules")
    public List<RuleDto> rules;
}