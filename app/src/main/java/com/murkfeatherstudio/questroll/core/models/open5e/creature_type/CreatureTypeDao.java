package com.murkfeatherstudio.questroll.core.models.open5e.creature_type;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;

import com.murkfeatherstudio.questroll.core.database.base.BaseDao;

import java.util.List;

@Dao
public interface CreatureTypeDao extends BaseDao<CreatureTypeEntity> {
    @Query("SELECT * FROM creature_types ORDER BY name ASC")
    LiveData<List<CreatureTypeEntity>> getAll();

    @Query("SELECT * FROM creature_types WHERE key = :key")
    LiveData<CreatureTypeEntity> getByKey(String key);

    @Query("DELETE FROM creature_types")
    void deleteAll();

    @Query("SELECT DISTINCT document_name FROM creature_types WHERE document_name IS NOT NULL ORDER BY document_name ASC")
    LiveData<List<String>> getDistinctSources();

    @Query("SELECT * FROM creature_types WHERE " +
            "(:query = '' OR name LIKE '%' || :query || '%') AND " +
            "(:source = '' OR document_name = :source) " +
            "ORDER BY name ASC")
    LiveData<List<CreatureTypeEntity>> getFiltered(String query, String source);
}
