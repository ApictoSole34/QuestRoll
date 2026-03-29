package com.fizzycoyote.qusetroll.core.models.open5e.damage_type;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface DamageTypeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<DamageTypeEntity> types);

    @Query("SELECT COUNT(*) FROM damage_types")
    int getCount();

    @Query("DELETE FROM damage_types")
    void deleteAll();

    @Query("SELECT * FROM damage_types ORDER BY name ASC")
    LiveData<List<DamageTypeEntity>> getAll();

    @Query("SELECT * FROM damage_types WHERE key = :key")
    LiveData<DamageTypeEntity> getByKey(String key);
}