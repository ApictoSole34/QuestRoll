package com.fizzycoyote.qusetroll.core.models.open5e.spell_school;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "spell_schools")
public class SpellSchoolEntity {
    @PrimaryKey @NonNull public String slug;
    public String url;
    public String name;
    public String description;
    public String documentUrl;
}
