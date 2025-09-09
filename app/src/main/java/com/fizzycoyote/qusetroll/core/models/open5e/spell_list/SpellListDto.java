package com.fizzycoyote.qusetroll.core.models.open5e.spell_list;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class SpellListDto implements Serializable {
    @SerializedName("slug") public String slug;
    @SerializedName("name") public String name;
    @SerializedName("desc") public String desc;
    @SerializedName("spells") public String spells;
    @SerializedName("document_slug") public String documentSlug;
    @SerializedName("document_title") public String documentTitle;
    @SerializedName("document_license_url") public String documentLicenseUrl;
    @SerializedName("document_url") public String documentUrl;
}
