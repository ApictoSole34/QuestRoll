package com.fizzycoyote.qusetroll.core.models.open5e.character_class.saving_throw;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface SavingThrowDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertSavingThrows(List<SavingThrowEntity> savingThrows);

    @Query("DELETE FROM saving_throws WHERE class_key_ref = :classKey")
    void deleteSavingThrowsForClass(String classKey);
}