package com.fizzycoyote.qusetroll.core.models.open5e.spell_school;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface SpellSchoolDao {
    @Query("SELECT * FROM spell_schools ORDER BY name ASC")
    List<SpellSchoolEntity> getAllSchools();

    @Query("SELECT * FROM spell_schools ORDER BY name ASC")
    LiveData<List<SpellSchoolEntity>> getAllSchoolsLive();

    @Query("SELECT * FROM spell_schools WHERE `key` = :key")
    LiveData<SpellSchoolEntity> getByKey(String key);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<SpellSchoolEntity> schools);
}