package com.fizzycoyote.qusetroll.core.models.open5e.item_set;

import androidx.annotation.Nullable;

import com.fizzycoyote.qusetroll.core.models.open5e.item.ItemDto;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class ItemSetDto implements Serializable {
    @SerializedName("url") public String url;
    @SerializedName("key") public String key;
    @SerializedName("items") public List<ItemDto> items;
    @SerializedName("name") public String name;
    @SerializedName("desc") @Nullable public String desc;
    @SerializedName("document") public String document;
}
