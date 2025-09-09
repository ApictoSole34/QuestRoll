package com.fizzycoyote.qusetroll.core.models.open5e.item_category;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class ItemCategoryDto implements Serializable {
    @SerializedName("url") public String url;
    @SerializedName("key") public String key;
    @SerializedName("name") public String name;
    @SerializedName("document") public String document;
}
