package com.fizzycoyote.qusetroll.core.models.custom.custom_environment;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface CustomEnvironmentDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(CustomEnvironmentEntity env);

    @Update
    void update(CustomEnvironmentEntity env);

    @Query("DELETE FROM custom_environments WHERE id = :id")
    void delete(long id);

    @Query("SELECT * FROM custom_environments ORDER BY name ASC")
    LiveData<List<CustomEnvironmentEntity>> getAll();

    @Query("SELECT * FROM custom_environments WHERE id = :id")
    LiveData<CustomEnvironmentEntity> getById(long id);

    @Query("SELECT * FROM custom_environments WHERE id = :id")
    CustomEnvironmentEntity getByIdSync(long id);

    @Query("SELECT COUNT(*) FROM custom_environments WHERE name = :name")
    int countByName(String name);
}