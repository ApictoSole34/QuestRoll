package com.murkfeatherstudio.questroll.core.models.open5e.item_category;

import com.google.gson.annotations.SerializedName;

public class ItemCategoryDto {
    @SerializedName("key") public String key;
    @SerializedName("name") public String name;
    @SerializedName("url") public String url;
    @SerializedName("document") public DocumentDto document;

    public static class DocumentDto {
        @SerializedName("name") public String name;
        @SerializedName("key") public String key;
        @SerializedName("type") public String type;
        @SerializedName("display_name") public String displayName;
        @SerializedName("publisher") public PublisherDto publisher;
        @SerializedName("gamesystem") public GameSystemDto gamesystem;
        @SerializedName("permalink") public String permalink;
    }

    public static class PublisherDto {
        @SerializedName("name") public String name;
        @SerializedName("key") public String key;
        @SerializedName("url") public String url;
    }

    public static class GameSystemDto {
        @SerializedName("name") public String name;
        @SerializedName("key") public String key;
        @SerializedName("url") public String url;
    }
}