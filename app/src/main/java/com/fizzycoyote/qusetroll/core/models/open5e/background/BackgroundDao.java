package com.fizzycoyote.qusetroll.core.models.open5e.background;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface BackgroundDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<BackgroundEntity> backgrounds);

    @Query("SELECT COUNT(*) FROM backgrounds")
    int getCount();

    @Query("DELETE FROM backgrounds")
    void deleteAll();

    @Query("SELECT * FROM backgrounds WHERE " +
            "(:query = '' OR name LIKE '%' || :query || '%') AND " +
            "(:source = '' OR document_name LIKE '%' || :source || '%') " +
            "ORDER BY name ASC")
    LiveData<List<BackgroundEntity>> getFiltered(String query, String source);

    @Query("SELECT * FROM backgrounds WHERE key = :key")
    LiveData<BackgroundEntity> getByKey(String key);

    @Query("SELECT DISTINCT document_name FROM backgrounds WHERE document_name IS NOT NULL ORDER BY document_name ASC")
    List<String> getDistinctSources();
}