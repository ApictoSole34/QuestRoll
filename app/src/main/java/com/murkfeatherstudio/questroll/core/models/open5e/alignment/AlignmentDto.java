package com.murkfeatherstudio.questroll.core.models.open5e.alignment;

import com.murkfeatherstudio.questroll.core.models.open5e.document.DocumentDto;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class AlignmentDto {
    @SerializedName("key") public String key;
    @SerializedName("morality") public String morality;
    @SerializedName("societal_attitude") public String societalAttitude;
    @SerializedName("short_name") public String shortName;
    @SerializedName("descriptions") public List<Description> descriptions;
    @SerializedName("document") public DocumentDto document;

    public static class Description {
        @SerializedName("desc") public String desc;
        @SerializedName("document") public String document;
        @SerializedName("gamesystem") public String gamesystem;
    }
}