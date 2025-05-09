package com.fizzycoyote.qusetroll.core.models.open5e.license;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface LicenseDao {
    @Query("SELECT * FROM licenses WHERE 'key' = :key LIMIT 1")
    LicenseEntity getByKey(String key);

    @Query("SELECT * FROM licenses")
    List<LicenseEntity> getAllLicenses();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<LicenseEntity> licenses);
}
