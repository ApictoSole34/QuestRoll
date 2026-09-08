package com.murkfeatherstudio.questroll.core.models.open5e.species;

import com.murkfeatherstudio.questroll.core.models.open5e.document.DocumentDto;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class SpeciesDto implements Serializable {
    @SerializedName("key") public String key;
    @SerializedName("name") public String name;
    @SerializedName("desc") public String desc;
    @SerializedName("is_subspecies") public boolean isSubspecies;
    @SerializedName("subspecies_of") public String subspeciesOf;
    @SerializedName("document") public DocumentDto document;
    @SerializedName("traits") public List<SpeciesTraitDto> traits;

    public static class SpeciesTraitDto implements Serializable {
        @SerializedName("name") public String name;
        @SerializedName("desc") public String desc;
        @SerializedName("type") public String type;
        @SerializedName("order") public Integer order;
    }
}
