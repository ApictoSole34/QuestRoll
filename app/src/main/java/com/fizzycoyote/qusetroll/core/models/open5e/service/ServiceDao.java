package com.fizzycoyote.qusetroll.core.models.open5e.service;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface ServiceDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<ServiceEntity> services);

    @Query("SELECT COUNT(*) FROM services")
    int getCount();

    @Query("DELETE FROM services")
    void deleteAll();

    @Query("SELECT * FROM services ORDER BY name ASC")
    LiveData<List<ServiceEntity>> getAll();

    @Query("SELECT * FROM services WHERE key = :key")
    LiveData<ServiceEntity> getByKey(String key);
}
