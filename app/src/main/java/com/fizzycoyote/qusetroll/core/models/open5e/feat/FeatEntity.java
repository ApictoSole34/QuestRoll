package com.fizzycoyote.qusetroll.core.models.open5e.feat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "feats")
public class FeatEntity {
    @PrimaryKey @NonNull public String key;
    public String url;
    public boolean hasPrerequisite;
    public String document;
    public String name;
    public String desc;
    @Nullable public String prerequisites;
}
