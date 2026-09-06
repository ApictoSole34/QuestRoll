package com.fizzycoyote.qusetroll.core.models.open5e.creature_type;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import java.util.List;

/**
 * Data Access Object for D&D 5e creature types (e.g., Beast, Undead, Dragon)
 * stored in the compendium.
 * <p>
 * Provides methods for filtering creature types and retrieving their metadata.
 * </p>
 */
@Dao
public interface CreatureTypeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<CreatureTypeEntity> types);

    @Query("SELECT COUNT(*) FROM creature_types")
    int getCount();

    @Query("DELETE FROM creature_types")
    void deleteAll();

    @Query("SELECT * FROM creature_types")
    List<CreatureTypeEntity> getAllSync();

    /**
     * Retrieves all creature types ordered by name.
     *
     * @return LiveData list of {@link CreatureTypeEntity}.
     */
    @Query("SELECT * FROM creature_types ORDER BY name ASC")
    LiveData<List<CreatureTypeEntity>> getAll();

    /**
     * Retrieves a specific creature type by its unique key.
     */
    @Query("SELECT * FROM creature_types WHERE key = :key")
    LiveData<CreatureTypeEntity> getByKey(String key);

    /**
     * Searches and filters creature types by name and source document.
     *
     * @param query  Partial name to match.
     * @param source Exact document name to filter by.
     * @return LiveData containing the filtered list of creature types.
     */
    @Query("SELECT * FROM creature_types WHERE " +
            "(:query = '' OR name LIKE '%' || :query || '%') AND " +
            "(:source = '' OR document_name = :source) " +
            "ORDER BY name ASC")
    LiveData<List<CreatureTypeEntity>> getFiltered(String query, String source);

    /**
     * Retrieves a list of unique source document names present in the creature type database.
     */
    @Query("SELECT DISTINCT document_name FROM creature_types WHERE document_name IS NOT NULL ORDER BY document_name ASC")
    LiveData<List<String>> getDistinctSources();
}
