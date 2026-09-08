package com.murkfeatherstudio.questroll.core.models.custom.custom_damage_types;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface CustomDamageTypeDao {
    @Insert long insert(CustomDamageTypeEntity type);
    @Update
    void update(CustomDamageTypeEntity type);
    @Query("DELETE FROM custom_damage_types WHERE id = :id") void delete(long id);
    @Query("SELECT * FROM custom_damage_types ORDER BY name ASC") LiveData<List<CustomDamageTypeEntity>> getAll();
    @Query("SELECT * FROM custom_damage_types WHERE id = :id") LiveData<CustomDamageTypeEntity> getById(long id);
    @Query("SELECT COUNT(*) FROM custom_damage_types WHERE name = :name") int countByName(String name);
}