package com.fizzycoyote.qusetroll.core.models.open5e.environment;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface EnvironmentDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<EnvironmentEntity> environments);

    @Query("SELECT COUNT(*) FROM environments")
    int getCount();

    @Query("DELETE FROM environments")
    void deleteAll();

    @Query("SELECT * FROM environments ORDER BY name ASC")
    LiveData<List<EnvironmentEntity>> getAll();

    @Query("SELECT * FROM environments WHERE key = :key")
    LiveData<EnvironmentEntity> getByKey(String key);
}
