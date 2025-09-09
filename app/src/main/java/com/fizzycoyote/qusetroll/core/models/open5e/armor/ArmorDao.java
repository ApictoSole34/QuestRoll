package com.fizzycoyote.qusetroll.core.models.open5e.armor;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface ArmorDao {
    @Query("SELECT * FROM armor")
    List<ArmorEntity> getAllArmor();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<ArmorEntity> armorList);
}
