package com.fizzycoyote.qusetroll.core.models.open5e.creature;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface CreatureDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<CreatureEntity> creatures);

    @Query("SELECT COUNT(*) FROM creatures")
    int getCount();

    @Query("DELETE FROM creatures")
    void deleteAll();

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

    @Query("SELECT * FROM creatures WHERE key = :key")
    LiveData<CreatureEntity> getByKey(String key);

    @Query("SELECT DISTINCT document_name FROM creatures WHERE document_name IS NOT NULL ORDER BY document_name ASC")
    List<String> getDistinctSources();
}
