package com.fizzycoyote.qusetroll.core.models.custom.custom_species;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface CustomSpeciesDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(CustomSpeciesEntity species);

    @Update
    void update(CustomSpeciesEntity species);

    @Query("DELETE FROM custom_species WHERE id = :id")
    void delete(long id);

    @Query("SELECT * FROM custom_species ORDER BY name ASC")
    LiveData<List<CustomSpeciesEntity>> getAll();

    @Query("SELECT * FROM custom_species WHERE id = :id")
    LiveData<CustomSpeciesEntity> getById(long id);

    @Query("SELECT * FROM custom_species WHERE id = :id")
    CustomSpeciesEntity getByIdSync(long id);

    @Query("SELECT COUNT(*) FROM custom_species WHERE name = :name")
    int countByName(String name);
}