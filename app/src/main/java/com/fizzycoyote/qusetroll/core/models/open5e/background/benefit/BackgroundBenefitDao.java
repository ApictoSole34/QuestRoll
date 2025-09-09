package com.fizzycoyote.qusetroll.core.models.open5e.background.benefit;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface BackgroundBenefitDao {
    @Query("SELECT * FROM benefits WHERE backgroundKey = :bgKey")
    List<BackgroundBenefitEntity> getBenefitsForBackground(String bgKey);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<BackgroundBenefitEntity> benefits);
}
