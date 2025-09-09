package com.fizzycoyote.qusetroll.core.models.open5e.race.trait;


import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface TraitDao {
    @Query("SELECT * FROM traits WHERE raceKey = :raceKey")
    List<TraitEntity> getTraitsForRace(String raceKey);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<TraitEntity> traits);
}
