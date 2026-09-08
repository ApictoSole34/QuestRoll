package com.murkfeatherstudio.questroll.core.models.open5e.condition;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;

import com.murkfeatherstudio.questroll.core.database.base.BaseDao;

import java.util.List;

/**
 * Data Access Object for D&D 5e conditions (e.g., Blinded, Prone) stored in the compendium.
 */
@Dao
public interface ConditionDao extends BaseDao<ConditionEntity> {

    @Query("SELECT COUNT(*) FROM conditions")
    int getCount();

    @Query("DELETE FROM conditions")
    void deleteAll();

    /**
     * Retrieves all conditions ordered by name as LiveData.
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
