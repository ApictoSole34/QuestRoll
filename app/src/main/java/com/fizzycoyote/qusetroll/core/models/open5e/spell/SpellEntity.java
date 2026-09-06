package com.fizzycoyote.qusetroll.core.models.open5e.spell;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.fizzycoyote.qusetroll.core.models.open5e.Converters;

import java.util.List;

/**
 * Entity representing a D&D 5e spell as stored in the local cache.
 * <p>
 * This class stores comprehensive spell details including casting time, duration,
 * range, components (V, S, M), and school. It is used for both displaying spell
 * lists and looking up spell mechanics during gameplay.
 * </p>
 */
@Entity(tableName = "spells", indices = {
        @Index("name"),
        @Index("level"),
        @Index("school_key")
})
public class SpellEntity {

    /**
     * Unique identifier for the spell (e.g., "fireball").
     */
    @PrimaryKey
    @NonNull
    public String key;

    public String name;
    public String desc;
    public int level;

    @ColumnInfo(name = "higher_level")
    public String higherLevel;

    @ColumnInfo(name = "school_name")
    public String schoolName;

    @ColumnInfo(name = "school_key")
    public String schoolKey;

    /**
     * List of class keys that have access to this spell.
     */
    @TypeConverters(Converters.class)
    public List<String> classes;

    @ColumnInfo(name = "casting_time")
    public String castingTime;

    public String duration;

    @ColumnInfo(name = "range_text")
    public String rangeText;

    public float range;

    public boolean ritual;
    public boolean concentration;
    public boolean verbal;
    public boolean somatic;
    public boolean material;

    @ColumnInfo(name = "material_specified")
    public String materialSpecified;

    @ColumnInfo(name = "reaction_condition")
    public String reactionCondition;

    @ColumnInfo(name = "target_type")
    public String targetType;

    @ColumnInfo(name = "target_count")
    public int targetCount;

    @ColumnInfo(name = "saving_throw_ability")
    public String savingThrowAbility;

    @ColumnInfo(name = "attack_roll")
    public boolean attackRoll;

    @ColumnInfo(name = "damage_roll")
    public String damageRoll;

    @TypeConverters(Converters.class)
    @ColumnInfo(name = "damage_types")
    public List<String> damageTypes;

    @ColumnInfo(name = "casting_options_json")
    public String castingOptionsJson;

    @ColumnInfo(name = "document_name")
    public String documentName;

    @ColumnInfo(name = "document_key")
    public String documentKey;
}
