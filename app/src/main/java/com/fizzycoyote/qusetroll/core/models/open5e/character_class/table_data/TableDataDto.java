package com.fizzycoyote.qusetroll.core.models.open5e.character_class.table_data;

import com.google.gson.annotations.SerializedName;

public class TableDataDto {
    @SerializedName("level") public int level;
    @SerializedName("column_value") public String columnValue;
}