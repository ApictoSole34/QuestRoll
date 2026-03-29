package com.fizzycoyote.qusetroll.core.models.open5e.ability.skill;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface SkillDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<SkillEntity> list);

    @Query("DELETE FROM skills")
    void deleteAll();

    @Query("SELECT * FROM skills ORDER BY name ASC")
    LiveData<List<SkillEntity>> getAll();

    @Query("SELECT * FROM skills WHERE abilityKey = :abilityKey ORDER BY name ASC")
    LiveData<List<SkillEntity>> getByAbility(String abilityKey);

    @Query("SELECT * FROM skills WHERE key = :key")
    LiveData<SkillEntity> getByKey(String key);
}