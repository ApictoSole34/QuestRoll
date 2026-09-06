package com.fizzycoyote.qusetroll.core.models.open5e.license;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

/**
 * Data Access Object for D&D 5e licenses (e.g., OGL, Creative Commons) stored in the compendium.
 * <p>
 * This DAO provides methods to access license text and information for various
 * source documents.
 * </p>
 */
@Dao
public interface LicenseDao {
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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<LicenseEntity> licenses);
}
