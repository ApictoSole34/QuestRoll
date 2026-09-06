package com.fizzycoyote.qusetroll.core.models.open5e.condition;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import java.util.List;

/**
 * Data Access Object for D&D 5e conditions (e.g., Blinded, Prone) stored in the compendium.
 * <p>
 * Provides methods for filtering conditions by name and source document.
 * </p>
 */
@Dao
public interface ConditionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<ConditionEntity> conditions);

    @Query("SELECT COUNT(*) FROM conditions")
    int getCount();

    @Query("DELETE FROM conditions")
    void deleteAll();

    /**
     * Retrieves all conditions ordered by name as LiveData.
     *
     * @return LiveData list of {@link ConditionEntity}.
     */
    @Query("SELECT * FROM conditions ORDER BY name ASC")
    LiveData<List<ConditionEntity>> getAll();

    /**
     * Retrieves a specific condition by its unique key.
     */
    @Query("SELECT * FROM conditions WHERE key = :key")
    LiveData<ConditionEntity> getByKey(String key);

    /**
     * Searches and filters conditions by name and source document.
     *
     * @param query  Partial name to match.
     * @param source Exact document name to filter by.
     * @return LiveData containing the filtered list of conditions.
     */
    @Query("SELECT * FROM conditions WHERE " +
            "(:query = '' OR name LIKE '%' || :query || '%') AND " +
            "(:source = '' OR document_name = :source) " +
            "ORDER BY name ASC")
    LiveData<List<ConditionEntity>> getFiltered(String query, String source);

    /**
     * Retrieves a list of unique source document names present in the condition database.
     */
    @Query("SELECT DISTINCT document_name FROM conditions WHERE document_name IS NOT NULL ORDER BY document_name ASC")
    LiveData<List<String>> getDistinctSources();
}
