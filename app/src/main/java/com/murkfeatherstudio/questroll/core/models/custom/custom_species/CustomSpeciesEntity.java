package com.murkfeatherstudio.questroll.core.models.custom.custom_species;

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

    @ColumnInfo(name = "speed")
    public String speed = "";

    @ColumnInfo(name = "size")
    public String size = "";

    @ColumnInfo(name = "ability_bonuses_json")
    public String abilityBonusesJson = "[]";

    @ColumnInfo(name = "language_keys_json")
    public String languageKeysJson = "[]";

    @ColumnInfo(name = "language_choices")
    public int languageChoices = 0;

    @ColumnInfo(name = "other_traits_json")
    public String otherTraitsJson = "[]";

    @ColumnInfo(name = "game_system")
    public String gameSystem = "5e-2014";
}
