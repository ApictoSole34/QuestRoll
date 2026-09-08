package com.murkfeatherstudio.questroll.core.models.open5e.rule;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "rules", indices = {@Index("rulesetKey"), @Index("key")})
public class RuleEntity {
    @PrimaryKey
    @NonNull
    public String key;
    public String name;
    public String desc;
    public int index;
    public int initialHeaderLevel;
    public String documentUrl;
    public String rulesetKey;

}