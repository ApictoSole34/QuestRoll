package com.fizzycoyote.qusetroll.core.models.open5e.item_category;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class ItemCategoryResponse implements Serializable {
    @SerializedName("count") public int count;
    @SerializedName("next") public String next;
    @SerializedName("previous") public String previous;
    @SerializedName("results") public List<ItemCategoryDto> results;
}
