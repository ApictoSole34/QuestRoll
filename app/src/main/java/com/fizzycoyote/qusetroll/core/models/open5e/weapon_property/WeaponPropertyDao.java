package com.fizzycoyote.qusetroll.core.models.open5e.weapon_property;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface WeaponPropertyDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<WeaponPropertyEntity> properties);

    @Query("SELECT COUNT(*) FROM weapon_properties")
    int getCount();

    @Query("DELETE FROM weapon_properties")
    void deleteAll();

    @Query("SELECT * FROM weapon_properties ORDER BY name ASC")
    LiveData<List<WeaponPropertyEntity>> getAll();

    @Query("SELECT * FROM weapon_properties WHERE key = :key")
    LiveData<WeaponPropertyEntity> getByKey(String key);

    @Query("SELECT * FROM weapon_properties WHERE name = :name LIMIT 1")
    LiveData<WeaponPropertyEntity> getByName(String name);
}