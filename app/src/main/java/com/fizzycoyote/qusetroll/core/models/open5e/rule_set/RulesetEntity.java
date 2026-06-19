package com.fizzycoyote.qusetroll.core.models.open5e.rule_set;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "rulesets")
public class RulesetEntity {

    @PrimaryKey
    @NonNull
    public String key;

    public String name;

    public String desc;

    public String documentName;

    public String documentKey;
}