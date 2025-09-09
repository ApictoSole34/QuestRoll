package com.fizzycoyote.qusetroll.core.models.open5e.spell;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.fizzycoyote.qusetroll.core.models.open5e.Converters;

import java.util.List;

@Entity(tableName = "spells")
@TypeConverters({Converters.class})
public class SpellEntity {
    @PrimaryKey @NonNull public String key;
    public String url;
    public String document;
    public List<CastingOptionDto> castingOptions;
    public String school;
    public List<String> classes;
    public String rangeUnit;
    public String shapeSizeUnit;
    public String name;
    public String desc;
    public int level;
    public String higherLevel;
    public String targetType;
    public String rangeText;
    public float range;
    public boolean ritual;
    public String castingTime;
    public String reactionCondition;
    public boolean verbal;
    public boolean somatic;
    public boolean material;
    public String materialSpecified;
    public String materialCost;
    public boolean materialConsumed;
    public int targetCount;
    public String savingThrowAbility;
    public boolean attackRoll;
    public String damageRoll;
    public List<String> damageTypes;
    public String duration;
    public String shapeType;
    public Float shapeSize;
    public boolean concentration;
}
