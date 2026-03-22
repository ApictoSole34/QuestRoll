package com.fizzycoyote.qusetroll.core.models.open5e.weapon;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.fizzycoyote.qusetroll.core.models.open5e.Converters;

import java.util.List;

@Entity(tableName = "weapons", indices = {
        @Index("name"),
        @Index("is_simple"),
        @Index("document_name")
})
public class WeaponEntity {

    @PrimaryKey
    @NonNull
    public String key;

    public String name;

    @ColumnInfo(name = "damage_dice") public String damageDice;
    @ColumnInfo(name = "damage_type_name") public String damageTypeName;
    @ColumnInfo(name = "damage_type_key") public String damageTypeKey;

    public float range;
    @ColumnInfo(name = "long_range") public float longRange;

    @ColumnInfo(name = "is_simple") public boolean isSimple;
    @ColumnInfo(name = "is_improvised") public boolean isImprovised;

    @ColumnInfo(name = "properties_json") public String propertiesJson;

    @ColumnInfo(name = "document_name") public String documentName;
    @ColumnInfo(name = "document_key") public String documentKey;
}