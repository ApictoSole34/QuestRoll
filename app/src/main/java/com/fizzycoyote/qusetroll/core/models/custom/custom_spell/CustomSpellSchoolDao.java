package com.fizzycoyote.qusetroll.core.models.custom.custom_spell;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface CustomSpellSchoolDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(CustomSpellSchoolEntity school);

    @Update
    void update(CustomSpellSchoolEntity school);

    @Query("DELETE FROM custom_spell_schools WHERE id = :id")
    void delete(long id);

    @Query("SELECT * FROM custom_spell_schools WHERE id = :id")
    LiveData<CustomSpellSchoolEntity> getById(long id);

    @Query("SELECT * FROM custom_spell_schools ORDER BY name ASC")
    LiveData<List<CustomSpellSchoolEntity>> getAll();

    @Query("SELECT * FROM custom_spell_schools ORDER BY name ASC")
    List<CustomSpellSchoolEntity> getAllSync();

    @Query("SELECT COUNT(*) FROM custom_spell_schools WHERE name = :name")
    int countByName(String name);
}
