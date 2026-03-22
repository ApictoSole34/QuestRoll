package com.fizzycoyote.qusetroll.core.models.custom.custom_weapon;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface CustomWeaponDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(CustomWeaponEntity weapon);

    @Update
    void update(CustomWeaponEntity weapon);

    @Query("DELETE FROM custom_weapons WHERE id = :id")
    void delete(long id);

    @Query("SELECT * FROM custom_weapons ORDER BY name ASC")
    LiveData<List<CustomWeaponEntity>> getAll();

    @Query("SELECT * FROM custom_weapons WHERE id = :id")
    LiveData<CustomWeaponEntity> getById(long id);

    @Query("SELECT * FROM custom_weapons WHERE id = :id")
    CustomWeaponEntity getByIdSync(long id);

    @Query("SELECT COUNT(*) FROM custom_weapons WHERE name = :name")
    int countByName(String name);
}