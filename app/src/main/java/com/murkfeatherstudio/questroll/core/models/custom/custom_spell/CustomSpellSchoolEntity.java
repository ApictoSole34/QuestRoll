package com.murkfeatherstudio.questroll.core.models.custom.custom_spell;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "custom_spell_schools")
public class CustomSpellSchoolEntity {
    @PrimaryKey(autoGenerate = true)
    public long id;

    @NonNull
    public String name;

    public String description;
}