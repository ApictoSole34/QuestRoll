package com.fizzycoyote.qusetroll.core.models.open5e.spell_school;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface SpellSchoolDao {
    @Query("SELECT * FROM spell_schools ORDER BY name ASC")
    List<SpellSchoolEntity> getAllSchools();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<SpellSchoolEntity> schools);
}
