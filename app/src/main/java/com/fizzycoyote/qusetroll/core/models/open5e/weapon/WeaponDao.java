package com.fizzycoyote.qusetroll.core.models.open5e.weapon;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface WeaponDao {
    @Query("SELECT * FROM weapons")
    List<WeaponEntity> getAllWeapons();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<WeaponEntity> weaponList);
}