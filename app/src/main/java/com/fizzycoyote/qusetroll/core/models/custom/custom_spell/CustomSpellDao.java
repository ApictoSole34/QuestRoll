package com.fizzycoyote.qusetroll.core.models.custom.custom_spell;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface CustomSpellDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(CustomSpellEntity spell);

    @Update
    void update(CustomSpellEntity spell);

    @Query("DELETE FROM custom_spells WHERE id = :id")
    void delete(long id);

    @Query("SELECT * FROM custom_spells ORDER BY level ASC, name ASC")
    LiveData<List<CustomSpellEntity>> getAll();

    @Query("SELECT * FROM custom_spells WHERE id = :id")
    LiveData<CustomSpellEntity> getById(long id);

    @Query("SELECT * FROM custom_spells WHERE id = :id")
    CustomSpellEntity getByIdSync(long id);

    @Query("SELECT * FROM custom_spells WHERE school_name = :schoolName")
    List<CustomSpellEntity> getBySchoolNameSync(String schoolName);

    @Query("SELECT COUNT(*) FROM custom_spells WHERE name = :name")
    int countByName(String name);
}
