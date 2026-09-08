package com.murkfeatherstudio.questroll.core.models.custom.custom_condition;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "custom_conditions")
public class CustomConditionEntity {
    @PrimaryKey(autoGenerate = true)
    public long id;
    public String name;
    public String description;
}
