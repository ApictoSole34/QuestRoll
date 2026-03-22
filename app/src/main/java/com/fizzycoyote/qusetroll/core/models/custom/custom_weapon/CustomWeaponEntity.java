package com.fizzycoyote.qusetroll.core.models.custom.custom_weapon;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "custom_weapons")
public class CustomWeaponEntity {

    @PrimaryKey(autoGenerate = true)
    public long id;

    @NonNull
    public String name = "";

    @ColumnInfo(name = "damage_dice") public String damageDice = "";
    @ColumnInfo(name = "damage_type_name") public String damageTypeName = "";

    public float range = 0f;
    @ColumnInfo(name = "long_range") public float longRange = 0f;

    @ColumnInfo(name = "is_simple") public boolean isSimple = true;
    @ColumnInfo(name = "is_improvised") public boolean isImprovised = false;

    // Properties jako JSON lista (name + desc)
    @ColumnInfo(name = "properties_json") public String propertiesJson = "";

    public String notes = "";
}