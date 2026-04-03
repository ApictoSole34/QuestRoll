package com.fizzycoyote.qusetroll.core.models.open5e.creature_type;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class CreatureTypeDto {
    @SerializedName("key") public String key;
    @SerializedName("name") public String name;
    @SerializedName("descriptions") public List<Description> descriptions;
    @SerializedName("document") public String document;
    @SerializedName("url") public String url;

    public static class Description {
        @SerializedName("desc") public String desc;
        @SerializedName("document") public String document;
        @SerializedName("gamesystem") public String gamesystem;
    }
}