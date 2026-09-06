package com.fizzycoyote.qusetroll.core.models.open5e.character_class;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import org.jspecify.annotations.Nullable;

/**
 * Entity representing a D&D 5e character class (e.g., Fighter, Wizard) as stored in the local cache.
 * <p>
 * This class captures basic information about the class, including its hit dice,
 * spellcasting type, and primary source document. Detailed progression and features
 * are stored in related entities.
 * </p>
 */
@Entity(tableName = "classes")
public class CharacterClassEntity {
    /**
     * Unique identifier for the class (e.g., "fighter").
     */
    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "class_key")
    public String key;

    public String name;
    public String document;

    /**
     * The type of spellcasting used by this class, if any (e.g., "full", "half").
     */
    @Nullable
    public String casterType;

    /**
     * If this is a subclass, the key of its parent class.
     */
    @ColumnInfo(name = "subclass_of_key")
    @Nullable
    public String subclassOfKey;

    public String hitDice;
    public String hitDiceName;
    public String hitPointsAt1stLevel;
    public String hitPointsAtHigherLevels;
}
