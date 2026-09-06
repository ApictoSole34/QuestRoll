package com.fizzycoyote.qusetroll.core.models.open5e.ability;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

/**
 * Data Access Object for D&D 5e abilities (e.g., Strength, Dexterity).
 * <p>
 * This DAO provides methods to access the core ability scores and their
 * descriptions stored in the local Open5e compendium cache.
 * </p>
 */
@Dao
public interface AbilityDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<AbilityEntity> list);

    @Query("DELETE FROM abilities")
    void deleteAll();

    /**
     * Retrieves all abilities ordered by name as LiveData.
     *
     * @return LiveData list of {@link AbilityEntity}.
     */
    @Query("SELECT * FROM abilities ORDER BY name ASC")
    LiveData<List<AbilityEntity>> getAll();

    /**
     * Synchronously retrieves all abilities.
     *
     * @return A list of all ability entities.
     */
    @Query("SELECT * FROM abilities ORDER BY name ASC")
    List<AbilityEntity> getAllSync();

    /**
     * Retrieves a specific ability by its key (e.g., "str").
     *
     * @param key The unique key of the ability.
     * @return LiveData containing the ability entity.
     */
    @Query("SELECT * FROM abilities WHERE key = :key")
    LiveData<AbilityEntity> getByKey(String key);

    @Query("SELECT * FROM abilities WHERE key = :key")
    AbilityEntity getByKeySync(String key);
}
