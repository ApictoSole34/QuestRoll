package com.fizzycoyote.qusetroll.core.models.custom.custom_character_class;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import com.fizzycoyote.qusetroll.core.models.custom.custom_character_class.custom_feature.CustomFeatureEntity;

import java.util.List;

@Dao
public interface CustomCharacterClassDao {

    @Query("SELECT COUNT(*) FROM custom_character_classes WHERE name = :name")
    int countByName(String name);

    @Query("DELETE FROM custom_features WHERE class_id = :classId")
    void deleteFeaturesForClass(long classId);

    @Query("DELETE FROM custom_character_classes WHERE id = :classId")
    void deleteClass(long classId);

    @Insert
    long insertClass(CustomCharacterClassEntity entity);

    @Insert
    void insertFeatures(List<CustomFeatureEntity> features);

    @Update
    void updateClass(CustomCharacterClassEntity entity);

    @Transaction
    @Query("SELECT * FROM custom_character_classes WHERE id = :id")
    CustomCharacterClassWithFeatures getClassWithFeaturesSync(long id);

    @Transaction
    @Query("SELECT * FROM custom_character_classes")
    LiveData<List<CustomCharacterClassWithFeatures>> getAllClasses();

    @Transaction
    @Query("SELECT * FROM custom_character_classes WHERE id = :classId")
    LiveData<CustomCharacterClassWithFeatures> getClassWithFeatures(long classId);

    @Query("SELECT * FROM custom_character_classes WHERE subclass_of = :parentKey")
    LiveData<List<CustomCharacterClassEntity>> getSubclasses(String parentKey);
}
