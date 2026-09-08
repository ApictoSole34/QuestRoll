package com.murkfeatherstudio.questroll.core.models.open5e.condition;

import com.murkfeatherstudio.questroll.core.models.open5e.document.DocumentDto;
import com.google.gson.annotations.SerializedName;
import java.util.List;

public class ConditionDto {
    @SerializedName("key") public String key;
    @SerializedName("name") public String name;
    @SerializedName("descriptions") public List<Description> descriptions;
    @SerializedName("document") public DocumentDto document;
    @SerializedName("url") public String url;

    public static class Description {
        @SerializedName("desc") public String desc;
        @SerializedName("document") public String document;
        @SerializedName("gamesystem") public String gamesystem;
    }
}