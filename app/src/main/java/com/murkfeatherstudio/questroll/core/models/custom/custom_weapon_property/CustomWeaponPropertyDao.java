package com.murkfeatherstudio.questroll.core.models.custom.custom_weapon_property;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface CustomWeaponPropertyDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(CustomWeaponPropertyEntity property);

    @Update
    void update(CustomWeaponPropertyEntity property);

    @Query("DELETE FROM custom_weapon_properties WHERE id = :id")
    void delete(long id);

    @Query("SELECT * FROM custom_weapon_properties ORDER BY name ASC")
    LiveData<List<CustomWeaponPropertyEntity>> getAll();

    @Query("SELECT * FROM custom_weapon_properties WHERE id = :id")
    LiveData<CustomWeaponPropertyEntity> getById(long id);

    @Query("SELECT * FROM custom_weapon_properties WHERE id = :id")
    CustomWeaponPropertyEntity getByIdSync(long id);

    @Query("SELECT COUNT(*) FROM custom_weapon_properties WHERE name = :name")
    int countByName(String name);
}