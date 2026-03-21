package com.fizzycoyote.qusetroll.core.models.custom.custom_creature;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface CustomCreatureTypeDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(CustomCreatureTypeEntity type);

    @Update
    void update(CustomCreatureTypeEntity type);

    @Query("DELETE FROM custom_creature_types WHERE id = :id")
    void delete(long id);

    @Query("SELECT * FROM custom_creature_types ORDER BY name ASC")
    LiveData<List<CustomCreatureTypeEntity>> getAll();

    @Query("SELECT * FROM custom_creature_types ORDER BY name ASC")
    List<CustomCreatureTypeEntity> getAllSync();

    @Query("SELECT COUNT(*) FROM custom_creature_types WHERE name = :name")
    int countByName(String name);
}