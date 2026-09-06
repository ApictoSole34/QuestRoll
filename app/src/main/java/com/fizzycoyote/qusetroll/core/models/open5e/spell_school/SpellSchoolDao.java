package com.fizzycoyote.qusetroll.core.models.open5e.spell_school;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

/**
 * Data Access Object for official D&D 5e spell schools (e.g., Abjuration, Evocation).
 * <p>
 * Provides access to the list of spell schools used to categorize spells in the compendium.
 * </p>
 */
@Dao
public interface SpellSchoolDao {
    /**
     * Synchronously retrieves all spell schools ordered by name.
     */
    @Query("SELECT * FROM spell_schools ORDER BY name ASC")
    List<SpellSchoolEntity> getAllSchools();

    /**
     * Retrieves all spell schools as LiveData.
     */
    @Query("SELECT * FROM spell_schools ORDER BY name ASC")
    LiveData<List<SpellSchoolEntity>> getAllSchoolsLive();

    /**
     * Retrieves a specific spell school by its unique key.
     */
    @Query("SELECT * FROM spell_schools WHERE `key` = :key")
    LiveData<SpellSchoolEntity> getByKey(String key);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<SpellSchoolEntity> schools);
}
