package com.fizzycoyote.qusetroll.core.models.open5e.ability.skill;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface SkillDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertSkills(List<SkillEntity> skills);

    @Query("SELECT * FROM skills WHERE abilityKey = :abilityKey")
    List<SkillEntity> getSkillsForAbility(String abilityKey);
}