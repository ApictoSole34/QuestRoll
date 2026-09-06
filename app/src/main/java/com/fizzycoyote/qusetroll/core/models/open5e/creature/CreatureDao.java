package com.fizzycoyote.qusetroll.core.models.open5e.creature;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

/**
 * Data Access Object for D&D 5e creatures (monsters) stored in the compendium.
 * <p>
 * This DAO provides advanced filtering logic to search for monsters by name, type,
 * alignment, Challenge Rating (CR) range, and source document.
 * </p>
 */
@Dao
public interface CreatureDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<CreatureEntity> creatures);

    @Query("SELECT COUNT(*) FROM creatures")
    int getCount();

    @Query("DELETE FROM creatures")
    void deleteAll();

    /**
     * Searches and filters creatures based on multiple criteria.
     *
     * @param query     Partial name to match.
     * @param typeKey   The unique key of the creature type.
     * @param alignment Partial alignment string to match.
     * @param crMin     Minimum Challenge Rating (-1 to ignore).
     * @param crMax     Maximum Challenge Rating (-1 to ignore).
     * @param source    Partial name of the source document.
     * @return LiveData containing the list of matching creatures, sorted by CR and name.
     */
    @Query("SELECT * FROM creatures WHERE " +
            "(:query = '' OR name LIKE '%' || :query || '%') AND " +
            "(:typeKey = '' OR type_key = :typeKey) AND " +
            "(:alignment = '' OR alignment LIKE '%' || :alignment || '%') AND " +
            "(:crMin < 0 OR challenge_rating_decimal >= :crMin) AND " +
            "(:crMax < 0 OR challenge_rating_decimal <= :crMax) AND " +
            "(:source = '' OR document_name LIKE '%' || :source || '%') " +
            "ORDER BY challenge_rating_decimal ASC, name ASC")
    LiveData<List<CreatureEntity>> getFilteredCreatures(
            String query,
            String typeKey,
            String alignment,
            float crMin,
            float crMax,
            String source
    );

    /**
     * Retrieves a specific creature by its unique key.
     *
     * @param key The creature key (e.g., "aboleth").
     * @return LiveData containing the creature entity.
     */
    @Query("SELECT * FROM creatures WHERE key = :key")
    LiveData<CreatureEntity> getByKey(String key);

    /**
     * Retrieves a list of unique source document names present in the creature database.
     *
     * @return A list of source names.
     */
    @Query("SELECT DISTINCT document_name FROM creatures WHERE document_name IS NOT NULL ORDER BY document_name ASC")
    List<String> getDistinctSources();
}
