package com.fizzycoyote.qusetroll.core.models.open5e.publisher;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class PublisherDto implements Serializable {
    @SerializedName("url") public String url;
    @SerializedName("key") public String key;
    @SerializedName("name") public String name;
}
