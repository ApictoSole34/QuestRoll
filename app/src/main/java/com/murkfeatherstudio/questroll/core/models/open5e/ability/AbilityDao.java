package com.murkfeatherstudio.questroll.core.models.open5e.ability;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;

import com.murkfeatherstudio.questroll.core.database.base.BaseDao;

import java.util.List;

/**
 * Data Access Object for D&D 5e abilities (e.g., Strength, Dexterity).
 */
@Dao
public interface AbilityDao extends BaseDao<AbilityEntity> {

    @Query("DELETE FROM abilities")
    void deleteAll();

    @Query("SELECT * FROM abilities ORDER BY name ASC")
    LiveData<List<AbilityEntity>> getAll();

    @Query("SELECT * FROM abilities ORDER BY name ASC")
    List<AbilityEntity> getAllSync();

    @Query("SELECT * FROM abilities WHERE key = :key")
    LiveData<AbilityEntity> getByKey(String key);

    @Query("SELECT * FROM abilities WHERE key = :key")
    AbilityEntity getByKeySync(String key);
}
