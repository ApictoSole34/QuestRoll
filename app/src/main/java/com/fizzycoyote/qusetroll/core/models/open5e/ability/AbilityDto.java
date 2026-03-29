package com.fizzycoyote.qusetroll.core.models.open5e.ability;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class AbilityDto {
    @SerializedName("key")       public String key;
    @SerializedName("name")      public String name;
    @SerializedName("short_desc") public String shortDesc;
    @SerializedName("descriptions") public List<AbilityDescriptionDto> descriptions;
    @SerializedName("skills")    public List<SkillDto> skills;

    public static class AbilityDescriptionDto {
        @SerializedName("desc")        public String desc;
        @SerializedName("document")    public String document;
        @SerializedName("gamesystem")  public String gamesystem;
    }

    public static class SkillDto {
        @SerializedName("key")          public String key;
        @SerializedName("name")         public String name;
        @SerializedName("ability")      public String ability;
        @SerializedName("document")     public String document;
        @SerializedName("descriptions") public List<AbilityDescriptionDto> descriptions;
    }
}