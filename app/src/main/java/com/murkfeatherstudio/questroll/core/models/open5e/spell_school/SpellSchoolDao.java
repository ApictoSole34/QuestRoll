package com.murkfeatherstudio.questroll.core.models.open5e.spell_school;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;

import com.murkfeatherstudio.questroll.core.database.base.BaseDao;

import java.util.List;

/**
 * Data Access Object for official D&D 5e spell schools.
 */
@Dao
public interface SpellSchoolDao extends BaseDao<SpellSchoolEntity> {
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
}
