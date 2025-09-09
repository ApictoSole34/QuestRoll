package com.fizzycoyote.qusetroll.core.models.open5e.ability;

import androidx.room.Dao;
import androidx.room.Embedded;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Relation;
import androidx.room.Transaction;

import com.fizzycoyote.qusetroll.core.models.open5e.ability.skill.SkillEntity;

import java.util.List;

@Dao
public interface AbilityDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAbilities(List<AbilityEntity> abilities);

    @Query("SELECT * FROM abilities WHERE 'key' = :key")
    AbilityEntity getAbility(String key);

    @Query("SELECT * FROM abilities")
    List<AbilityEntity> getAllAbilitiesSync();

    @Transaction
    @Query("SELECT * FROM abilities WHERE `key` = :key")
    AbilityWithSkills getAbilityWithSkills(String key);
}

