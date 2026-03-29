package com.fizzycoyote.qusetroll.core.models.custom.custom_ability;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface CustomSkillDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(CustomSkillEntity entity);

    @Update
    void update(CustomSkillEntity entity);

    @Delete
    void delete(CustomSkillEntity entity);

    @Query("SELECT * FROM custom_skills ORDER BY name ASC")
    LiveData<List<CustomSkillEntity>> getAll();

    @Query("""
        SELECT * FROM custom_skills
        WHERE abilityKey = :abilityKey AND parentIsCustom = :isCustom
        ORDER BY name ASC
    """)
    LiveData<List<CustomSkillEntity>> getByAbility(String abilityKey, boolean isCustom);

    @Query("SELECT * FROM custom_skills WHERE id = :id")
    LiveData<CustomSkillEntity> getById(long id);
}