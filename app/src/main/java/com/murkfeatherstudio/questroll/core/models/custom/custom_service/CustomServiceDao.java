package com.murkfeatherstudio.questroll.core.models.custom.custom_service;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface CustomServiceDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(CustomServiceEntity service);

    @Update
    void update(CustomServiceEntity service);

    @Query("DELETE FROM custom_services WHERE id = :id")
    void delete(long id);

    @Query("SELECT * FROM custom_services ORDER BY name ASC")
    LiveData<List<CustomServiceEntity>> getAll();

    @Query("SELECT * FROM custom_services WHERE id = :id")
    LiveData<CustomServiceEntity> getById(long id);

    @Query("SELECT * FROM custom_services WHERE id = :id")
    CustomServiceEntity getByIdSync(long id);

    @Query("SELECT COUNT(*) FROM custom_services WHERE name = :name")
    int countByName(String name);
}