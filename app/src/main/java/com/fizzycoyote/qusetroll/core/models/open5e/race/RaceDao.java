package com.fizzycoyote.qusetroll.core.models.open5e.race;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface RaceDao {
    @Query("SELECT * FROM races ORDER BY name ASC")
    List<RaceEntity> getAllRaces();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<RaceEntity> races);
}
