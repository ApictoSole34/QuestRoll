package com.fizzycoyote.qusetroll.core.models.open5e.armor;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "armor")
public class ArmorEntity {
    @PrimaryKey
    @NonNull
    public String key;

    public String url;
    public String name;
    public String category;
    public String acDisplay;
    public boolean grantsStealthDisadvantage;

    @Nullable
    public Integer strengthRequirement;

    public int acBase;
    public boolean acAddDexxmod;

    @Nullable
    public Integer acCapDexmod;
    public String document;
}
