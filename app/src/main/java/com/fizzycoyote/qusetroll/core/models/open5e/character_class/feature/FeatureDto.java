package com.fizzycoyote.qusetroll.core.models.open5e.character_class.feature;

import com.fizzycoyote.qusetroll.core.models.open5e.character_class.gained_at.GainedAtDto;
import com.fizzycoyote.qusetroll.core.models.open5e.character_class.table_data.TableDataDto;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class FeatureDto {
    @SerializedName("key") public String key;
    @SerializedName("name") public String name;
    @SerializedName("desc") public String desc;
    @SerializedName("feature_type") public String featureType;
    @SerializedName("gained_at") public List<GainedAtDto> gainedAt;
    @SerializedName("table_data") public List<TableDataDto> tableData;
}
