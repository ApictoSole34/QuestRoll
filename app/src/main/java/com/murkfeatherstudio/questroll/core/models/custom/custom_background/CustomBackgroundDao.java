package com.murkfeatherstudio.questroll.core.models.custom.custom_background;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface CustomBackgroundDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(CustomBackgroundEntity background);

    @Update
    void update(CustomBackgroundEntity background);

    @Query("SELECT * FROM custom_backgrounds WHERE game_system = :gameSystem ORDER BY name ASC")
    LiveData<List<CustomBackgroundEntity>> getByGameSystem(String gameSystem);

    @Query("SELECT * FROM custom_backgrounds WHERE game_system = :gameSystem ORDER BY name ASC")
    List<CustomBackgroundEntity> getByGameSystemSync(String gameSystem);

    @Query("DELETE FROM custom_backgrounds WHERE id = :id")
    void delete(long id);

    @Query("SELECT * FROM custom_backgrounds ORDER BY name ASC")
    LiveData<List<CustomBackgroundEntity>> getAll();

    @Query("SELECT * FROM custom_backgrounds WHERE id = :id")
    LiveData<CustomBackgroundEntity> getById(long id);

    @Query("SELECT * FROM custom_backgrounds WHERE id = :id")
    CustomBackgroundEntity getByIdSync(long id);

    @Query("SELECT COUNT(*) FROM custom_backgrounds WHERE name = :name")
    int countByName(String name);
}