package com.fizzycoyote.qusetroll.core.models.open5e.damage_type;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface DamageTypeDao {
    @Query("SELECT * FROM damage_types ORDER BY name ASC")
    List<DamageTypeEntity> getAllDamageTypes();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<DamageTypeEntity> damageTypes);
}