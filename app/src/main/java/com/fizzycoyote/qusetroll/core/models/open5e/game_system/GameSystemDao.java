package com.fizzycoyote.qusetroll.core.models.open5e.game_system;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

/**
 * Data Access Object for D&D 5e game systems (e.g., 5e-2014, 5e-2024) stored in the compendium.
 * <p>
 * This DAO manages the high-level system categories that group game documents and rules.
 * </p>
 */
@Dao
public interface GameSystemDao {
    /**
     * Retrieves a game system by its unique key.
     *
     * @param key The game system key.
     * @return The game system entity, or null if not found.
     */
    @Query("SELECT * FROM game_systems WHERE `key` = :key LIMIT 1")
    GameSystemEntity getByKey(String key);

    @Query("SELECT * FROM game_systems")
    List<GameSystemEntity> getAllGameSystems();

    /**
     * Synchronously retrieves all game systems ordered by name.
     */
    @Query("SELECT * FROM game_systems ORDER BY name ASC")
    List<GameSystemEntity> getAllSync();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<GameSystemEntity> gameSystems);
}
