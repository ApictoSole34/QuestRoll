package com.fizzycoyote.qusetroll.core.models.open5e.document;

import androidx.annotation.Nullable;

import com.fizzycoyote.qusetroll.core.models.open5e.license.LicenseDto;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;


public class DocumentDto implements Serializable {
    @SerializedName("url") public String url;
    @SerializedName("key") public String key;
    @SerializedName("licenses") public List<LicenseDto> licenses;
    @SerializedName("publisher") @Nullable public String publisher;
    @SerializedName("gamesystem") @Nullable public String gamesystem;
    @SerializedName("name") public String name;
    @SerializedName("desc") @Nullable public String desc;
    @SerializedName("author") public String author;
    @SerializedName("published_at") public String publishedAt;
    @SerializedName("permalink") public String permalink;
    @SerializedName("distance_unit") public String distanceUnit;
}
