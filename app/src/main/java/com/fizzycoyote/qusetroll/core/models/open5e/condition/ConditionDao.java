package com.fizzycoyote.qusetroll.core.models.open5e.condition;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import java.util.List;

@Dao
public interface ConditionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<ConditionEntity> conditions);

    @Query("SELECT COUNT(*) FROM conditions")
    int getCount();

    @Query("DELETE FROM conditions")
    void deleteAll();

    @Query("SELECT * FROM conditions ORDER BY name ASC")
    LiveData<List<ConditionEntity>> getAll();

    @Query("SELECT * FROM conditions WHERE key = :key")
    LiveData<ConditionEntity> getByKey(String key);

    @Query("SELECT * FROM conditions WHERE " +
            "(:query = '' OR name LIKE '%' || :query || '%') AND " +
            "(:source = '' OR document_name = :source) " +
            "ORDER BY name ASC")
    LiveData<List<ConditionEntity>> getFiltered(String query, String source);

    @Query("SELECT DISTINCT document_name FROM conditions WHERE document_name IS NOT NULL ORDER BY document_name ASC")
    LiveData<List<String>> getDistinctSources();
}