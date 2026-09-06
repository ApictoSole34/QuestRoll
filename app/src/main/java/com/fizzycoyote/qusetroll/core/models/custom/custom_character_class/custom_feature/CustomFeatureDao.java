package com.fizzycoyote.qusetroll.core.models.custom.custom_character_class.custom_feature;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface CustomFeatureDao {
    @Insert
    void insert(CustomFeatureEntity feature);

    @Insert
    void insertAll(List<CustomFeatureEntity> features);

    @Update
    void update(CustomFeatureEntity feature);

    @Delete
    void delete(CustomFeatureEntity feature);

    @Query("DELETE FROM custom_features WHERE class_id = :classId")
    void deleteFeaturesForClass(long classId);

    @Query("SELECT * FROM custom_features WHERE class_id = :classId")
    LiveData<List<CustomFeatureEntity>> getFeaturesForClass(long classId);

    @Query("SELECT * FROM custom_features WHERE class_id = :classId")
    List<CustomFeatureEntity> getFeaturesForClassSync(long classId);
}