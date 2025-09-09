package com.fizzycoyote.qusetroll.core.models.open5e.item;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.fizzycoyote.qusetroll.core.models.open5e.Converters;

import java.util.List;

@Entity(tableName = "items")
@TypeConverters(Converters.class)
public class ItemEntity {
    @PrimaryKey @NonNull public String key;
    public String url;
    public boolean isMagicItem;
    public String weaponUrl;
    public String armorUrl;
    public String document;
    public String category;
    public String rarity;
    public String name;
    public String desc;
    public String weight;
    public int armorClass;
    public int hitPoints;
    public String hitDice;
    public boolean nonmagicalAttackResistance;
    public boolean nonmagicalAttackImmunity;
    public String cost;
    public boolean requiresAttunement;
    public String size;
    public List<String> damageVulnerabilities;
    public List<String> damageImmunities;
    public List<String> damageResistances;
}
