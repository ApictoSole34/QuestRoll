package com.fizzycoyote.qusetroll.core.models.open5e.weapon;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.fizzycoyote.qusetroll.core.models.open5e.Converters;

import java.util.List;

@Entity(tableName = "weapons")
@TypeConverters(Converters.class)
public class WeaponEntity {
    @PrimaryKey
    @NonNull
    public String key;
    public String url;
    public String name;
    public String damageDice;
    public String versatileDice;
    public boolean isVersatile;
    public boolean isMartial;
    public boolean isMelee;
    public boolean rangedAttackPossible;
    public float reach;
    public float range;
    public float longRange;
    public boolean isFinesse;
    public boolean isThrown;
    public boolean isTwoHanded;
    public boolean requiresAmmunition;
    public boolean requiresLoading;
    public boolean isHeavy;
    public boolean isLight;
    public boolean isSimple;
    public boolean isImprovised;
    public List<String> properties;
    public String damageTypeUrl;
    public String documentUrl;
}
