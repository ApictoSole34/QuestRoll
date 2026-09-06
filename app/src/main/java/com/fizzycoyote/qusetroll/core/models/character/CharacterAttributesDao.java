package com.fizzycoyote.qusetroll.core.models.character;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

/**
 * Data Access Object for {@link CharacterAttributesEntity}.
 * <p>
 * Manages the storage and retrieval of character ability scores (Strength, Dexterity, etc.)
 * and their corresponding modifiers.
 * </p>
 */
@Dao
public interface CharacterAttributesDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(CharacterAttributesEntity attributes);

    @Update
    void update(CharacterAttributesEntity attributes);

    /**
     * Retrieves the attributes associated with a specific character.
     *
     * @param characterId The unique ID of the character.
     * @return The attributes entity, or null if not found.
     */
    @Query("SELECT * FROM character_attributes WHERE character_id = :characterId")
    CharacterAttributesEntity getByCharacterId(long characterId);
}
