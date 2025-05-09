package com.fizzycoyote.qusetroll.core.models.open5e.game_system;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface GameSystemDao {
    @Query("SELECT * FROM game_systems WHERE `key` = :key LIMIT 1")
    GameSystemEntity getByKey(String key);

    @Query("SELECT * FROM game_systems")
    List<GameSystemEntity> getAllGameSystems();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<GameSystemEntity> gameSystems);
}
