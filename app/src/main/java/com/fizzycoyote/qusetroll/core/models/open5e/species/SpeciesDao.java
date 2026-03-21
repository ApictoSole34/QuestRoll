package com.fizzycoyote.qusetroll.core.models.open5e.species;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface SpeciesDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<SpeciesEntity> species);

    @Query("SELECT COUNT(*) FROM species")
    int getCount();

    @Query("DELETE FROM species")
    void deleteAll();

    @Query("SELECT * FROM species WHERE " +
            "(:query = '' OR name LIKE '%' || :query || '%') AND " +
            "(:subspeciesOnly = 0 OR is_subspecies != 0) AND " +
            "(:mainOnly = 0 OR is_subspecies = 0) AND " +
            "(:source = '' OR document_name LIKE '%' || :source || '%') " +
            "ORDER BY name ASC")
    LiveData<List<SpeciesEntity>> getFilteredSpecies(
            String query,
            int subspeciesOnly,
            int mainOnly,
            String source
    );

    @Query("SELECT * FROM species WHERE key = :key")
    LiveData<SpeciesEntity> getByKey(String key);

    @Query("SELECT * FROM species WHERE subspecies_of LIKE '%' || :parentKey || '%' ORDER BY name ASC")
    LiveData<List<SpeciesEntity>> getSubspeciesOf(String parentKey);

    @Query("SELECT DISTINCT document_name FROM species WHERE document_name IS NOT NULL ORDER BY document_name ASC")
    List<String> getDistinctSources();
}