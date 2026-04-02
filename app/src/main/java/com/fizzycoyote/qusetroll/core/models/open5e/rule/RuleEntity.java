package com.fizzycoyote.qusetroll.core.models.open5e.rule;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "rules", indices = {@Index("rulesetKey")})
public class RuleEntity {
    @PrimaryKey
    @NonNull
    public String url;
    public String name;
    public String desc;
    public int index;
    public int initialHeaderLevel;
    public String documentUrl;
    public String rulesetKey;
}