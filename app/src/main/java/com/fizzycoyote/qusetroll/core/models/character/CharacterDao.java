package com.fizzycoyote.qusetroll.core.models.character;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import java.util.List;

/**
 * Data Access Object for the {@link CharacterEntity}.
 * <p>
 * Provides methods for basic CRUD operations on character records, as well as
 * specialized queries for fetching a character with all its related data
 * (attributes, classes, inventory, etc.) in a single transaction.
 * </p>
 */
@Dao
public interface CharacterDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(CharacterEntity character);

    @Update
    void update(CharacterEntity character);

    /**
     * Retrieves a character by its ID as LiveData.
     *
     * @param id The unique ID of the character.
     * @return LiveData containing the character entity.
     */
    @Query("SELECT * FROM characters WHERE id = :id")
    LiveData<CharacterEntity> getCharacter(long id);

    /**
     * Synchronously retrieves a character by its ID.
     *
     * @param id The unique ID of the character.
     * @return The character entity, or null if not found.
     */
    @Query("SELECT * FROM characters WHERE id = :id")
    CharacterEntity getCharacterSync(long id);

    /**
     * Synchronously retrieves all characters sorted by name.
     *
     * @return A list of all character entities.
     */
    @Query("SELECT * FROM characters ORDER BY name ASC")
    List<CharacterEntity> getAllCharactersSync();

    /**
     * Retrieves all characters sorted by name as LiveData.
     *
     * @return LiveData containing a list of character entities.
     */
    @Query("SELECT * FROM characters ORDER BY name ASC")
    LiveData<List<CharacterEntity>> getAllCharacters();

    /**
     * Retrieves a character along with all its related data (multiclassing, inventory,
     * traits, spells, etc.) in a single transaction.
     *
     * @param id The unique ID of the character.
     * @return LiveData containing the character and its relations.
     */
    @Transaction
    @Query("SELECT * FROM characters WHERE id = :id")
    LiveData<CharacterWithRelations> getCharacterWithRelations(long id);

    /**
     * Deletes a character by its ID.
     *
     * @param id The unique ID of the character to delete.
     */
    @Query("DELETE FROM characters WHERE id = :id")
    void deleteCharacter(long id);
}
