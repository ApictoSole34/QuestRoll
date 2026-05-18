package com.fizzycoyote.qusetroll.core.models.open5e.character_class.feature;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface FeatureDao {
    @Query("SELECT * FROM features WHERE class_key_ref = :classKey")
    LiveData<List<FeatureEntity>> getFeaturesForClass(String classKey);

    @Query("SELECT * FROM features WHERE class_key_ref = :classKey")
    List<FeatureEntity> getFeaturesForClassSync(String classKey);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertFeatures(List<FeatureEntity> features);

    @Query("DELETE FROM features WHERE class_key_ref = :classKey")
    void deleteFeaturesForClass(String classKey);
}