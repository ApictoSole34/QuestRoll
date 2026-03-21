package com.fizzycoyote.qusetroll.core.models.custom.custom_species;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "custom_species")
public class CustomSpeciesEntity {

    @PrimaryKey(autoGenerate = true)
    public long id;

    @NonNull
    public String name = "";
    public String desc = "";

    @ColumnInfo(name = "is_subspecies") public boolean isSubspecies = false;

    @ColumnInfo(name = "subspecies_of_key") public String subspeciesOfKey = "";
    @ColumnInfo(name = "subspecies_of_name") public String subspeciesOfName = "";

    @ColumnInfo(name = "traits_json") public String traitsJson = "";
}
