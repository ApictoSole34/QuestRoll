package com.murkfeatherstudio.questroll.core.models.character;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

/**
 * Data Access Object for {@link CharacterTraitEntity}.
 * <p>
 * Manages character-specific traits, which can originate from their race, class,
 * background, or other sources.
 * </p>
 */
@Dao
public interface CharacterTraitDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(CharacterTraitEntity trait);

    @Insert
    void insertAll(List<CharacterTraitEntity> traits);

    @Update
    void update(CharacterTraitEntity trait);

    /**
     * Retrieves all traits associated with a character, ordered by their display preference.
     *
     * @param characterId The ID of the character.
     * @return A list of character traits.
     */
    @Query("SELECT * FROM character_traits WHERE character_id = :characterId ORDER BY display_order ASC")
    List<CharacterTraitEntity> getByCharacterId(long characterId);

    /**
     * Removes all traits for a specific character.
     */
    @Query("DELETE FROM character_traits WHERE character_id = :characterId")
    void deleteForCharacter(long characterId);
}
