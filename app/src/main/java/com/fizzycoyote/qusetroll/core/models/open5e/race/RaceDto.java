package com.fizzycoyote.qusetroll.core.models.open5e.race;

import androidx.annotation.Nullable;

import com.fizzycoyote.qusetroll.core.models.open5e.race.trait.TraitDto;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class RaceDto implements Serializable {
    @SerializedName("url") public String url;
    @SerializedName("key") public String key;
    @SerializedName("is_subrace") public boolean isSubrace;
    @SerializedName("document") public String document;
    @SerializedName("traits") public List<TraitDto> traits;
    @SerializedName("name") public String name;
    @SerializedName("desc") public String desc;
    @SerializedName("subrace_of") @Nullable public String subraceOf;
}
