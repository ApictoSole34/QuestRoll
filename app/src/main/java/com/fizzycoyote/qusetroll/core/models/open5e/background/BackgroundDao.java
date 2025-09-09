package com.fizzycoyote.qusetroll.core.models.open5e.background;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface BackgroundDao {
    @Query("SELECT * FROM backgrounds ORDER BY name ASC")
    List<BackgroundEntity> getAllBackgrounds();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<BackgroundEntity> backgrounds);
}