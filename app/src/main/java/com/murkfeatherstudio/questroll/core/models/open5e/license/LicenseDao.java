package com.murkfeatherstudio.questroll.core.models.open5e.license;

import androidx.room.Dao;
import androidx.room.Query;

import com.murkfeatherstudio.questroll.core.database.base.BaseDao;

import java.util.List;

/**
 * Data Access Object for D&D 5e licenses (e.g., OGL, Creative Commons) stored in the compendium.
 */
@Dao
public interface LicenseDao extends BaseDao<LicenseEntity> {
    /**
     * Retrieves a license by its unique key.
     *
     * @param key The license key.
     * @return The license entity, or null if not found.
     */
    @Query("SELECT * FROM licenses WHERE `key` = :key LIMIT 1")
    LicenseEntity getByKey(String key);

    /**
     * Retrieves all licenses from the database.
     */
    @Query("SELECT * FROM licenses")
    List<LicenseEntity> getAllLicenses();
}
