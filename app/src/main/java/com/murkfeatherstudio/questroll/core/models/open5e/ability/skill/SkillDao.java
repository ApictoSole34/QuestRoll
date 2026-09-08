package com.murkfeatherstudio.questroll.core.models.open5e.ability.skill;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

/**
 * Data Access Object for D&D 5e skills (e.g., Athletics, Perception) stored in the compendium.
 * <p>
 * Skills are linked to a primary ability score (e.g., Athletics is a Strength skill).
 * </p>
 */
@Dao
public interface SkillDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<SkillEntity> list);

    @Query("DELETE FROM skills")
    void deleteAll();

    /**
     * Retrieves all skills ordered by name.
     *
     * @return LiveData containing the list of skill entities.
     */
    @Query("SELECT * FROM skills ORDER BY name ASC")
    LiveData<List<SkillEntity>> getAll();

    /**
     * Synchronously retrieves all skills.
     *
     * @return A list of all skill entities.
     */
    @Query("SELECT * FROM skills")
    List<SkillEntity> getAllSync();

    /**
     * Retrieves all skills associated with a specific game system.
     *
     * @param gameSystem The game system identifier.
     * @return A list of skills belonging to the system.
     */
    @Query("SELECT s.* FROM skills s " +
            "INNER JOIN documents d ON s.documentKey = d.key " +
            "WHERE d.gamesystem = :gameSystem " +
            "ORDER BY s.name ASC")
    List<SkillEntity> getAllByGameSystem(String gameSystem);

    /**
     * Retrieves all skills that are associated with a specific ability score.
     *
     * @param abilityKey The key of the ability (e.g., "str").
     * @return LiveData containing the list of associated skills.
     */
    @Query("SELECT * FROM skills WHERE abilityKey = :abilityKey ORDER BY name ASC")
    LiveData<List<SkillEntity>> getByAbility(String abilityKey);

    /**
     * Retrieves a specific skill by its unique key.
     *
     * @param key The skill key (e.g., "athletics").
     * @return LiveData containing the skill entity.
     */
    @Query("SELECT * FROM skills WHERE key = :key")
    LiveData<SkillEntity> getByKey(String key);
}
