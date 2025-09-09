package com.fizzycoyote.qusetroll.core.models.open5e.race;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "races")
public class RaceEntity {
    @PrimaryKey @NonNull public String key;
    public String url;
    public boolean isSubrace;
    public String document;
    public String name;
    public String desc;
    @Nullable public String subraceOf;
}
