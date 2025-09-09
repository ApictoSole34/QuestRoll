package com.fizzycoyote.qusetroll.core.models.open5e.feat.benefit;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface FeatBenefitDao {
    @Query("SELECT * FROM feat_benefits WHERE featKey = :featKey")
    List<FeatBenefitEntity> getBenefitsForFeat(String featKey);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<FeatBenefitEntity> benefits);
}
