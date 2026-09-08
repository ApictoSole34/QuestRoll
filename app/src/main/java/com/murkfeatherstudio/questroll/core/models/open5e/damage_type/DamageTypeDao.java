package com.murkfeatherstudio.questroll.core.models.open5e.damage_type;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;

import com.murkfeatherstudio.questroll.core.database.base.BaseDao;

import java.util.List;

/**
 * Data Access Object for D&D 5e damage types (e.g., Fire, Cold, Slashing)
 * stored in the compendium.
 */
@Dao
public interface DamageTypeDao extends BaseDao<DamageTypeEntity> {

    @Query("SELECT COUNT(*) FROM damage_types")
    int getCount();

    @Query("DELETE FROM damage_types")
    void deleteAll();

    /**
     * Retrieves all damage types ordered by name.
     *
     * @return LiveData list of {@link DamageTypeEntity}.
     */
    @Query("SELECT * FROM damage_types ORDER BY name ASC")
    LiveData<List<DamageTypeEntity>> getAll();

    /**
     * Retrieves a specific damage type by its unique key.
     */
    @Query("SELECT * FROM damage_types WHERE key = :key")
    LiveData<DamageTypeEntity> getByKey(String key);
}
