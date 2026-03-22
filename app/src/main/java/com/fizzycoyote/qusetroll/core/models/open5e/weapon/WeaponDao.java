package com.fizzycoyote.qusetroll.core.models.open5e.weapon;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface WeaponDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<WeaponEntity> weapons);

    @Query("SELECT COUNT(*) FROM weapons")
    int getCount();

    @Query("DELETE FROM weapons")
    void deleteAll();

    @Query("SELECT * FROM weapons WHERE " +
            "(:query = '' OR name LIKE '%' || :query || '%') AND " +
            "(:simpleOnly = 0 OR is_simple = 1) AND " +
            "(:martialOnly = 0 OR is_simple = 0) AND " +
            "(:source = '' OR document_name LIKE '%' || :source || '%') " +
            "ORDER BY name ASC")
    LiveData<List<WeaponEntity>> getFiltered(
            String query,
            int simpleOnly,
            int martialOnly,
            String source
    );

    @Query("SELECT * FROM weapons WHERE key = :key")
    LiveData<WeaponEntity> getByKey(String key);

    @Query("SELECT DISTINCT document_name FROM weapons WHERE document_name IS NOT NULL ORDER BY document_name ASC")
    List<String> getDistinctSources();
}