package com.murkfeatherstudio.questroll.core.models.open5e.creature;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;

import com.murkfeatherstudio.questroll.core.database.base.BaseDao;

import java.util.List;

/**
 * Data Access Object for D&D 5e creatures (monsters) stored in the compendium.
 */
@Dao
public interface CreatureDao extends BaseDao<CreatureEntity> {

    @Query("SELECT COUNT(*) FROM creatures")
    int getCount();

    @Query("DELETE FROM creatures")
    void deleteAll();

    /**
     * Searches and filters creatures based on multiple criteria.
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
     */
    @Query("SELECT * FROM creatures WHERE key = :key")
    LiveData<CreatureEntity> getByKey(String key);

    /**
     * Retrieves a list of unique source document names present in the creature database.
     */
    @Query("SELECT DISTINCT document_name FROM creatures WHERE document_name IS NOT NULL ORDER BY document_name ASC")
    List<String> getDistinctSources();
}
