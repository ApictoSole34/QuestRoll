package com.fizzycoyote.qusetroll.core.models.open5e.ability;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface AbilityDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<AbilityEntity> list);

    @Query("DELETE FROM abilities")
    void deleteAll();

    @Query("SELECT * FROM abilities ORDER BY name ASC")
    LiveData<List<AbilityEntity>> getAll();

    @Query("SELECT * FROM abilities WHERE key = :key")
    LiveData<AbilityEntity> getByKey(String key);

    @Query("SELECT * FROM abilities WHERE key = :key")
    AbilityEntity getByKeySync(String key);
}