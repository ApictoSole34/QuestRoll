package com.fizzycoyote.qusetroll.core.models.open5e.spell_list;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "spell_list")
public class SpellListEntity {
    @PrimaryKey
    @NonNull
    public String slug;

    public String name;
    public String desc;
    public String spells;
    public String documentSlug;
    public String documentTitle;
    public String documentLicenseUrl;
    public String documentUrl;
}
