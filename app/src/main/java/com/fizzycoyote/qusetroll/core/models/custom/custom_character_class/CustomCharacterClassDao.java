package com.fizzycoyote.qusetroll.core.models.custom.custom_character_class;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import com.fizzycoyote.qusetroll.core.models.custom.custom_character_class.custom_feature.CustomFeatureEntity;

import java.util.List;

@Dao
public interface CustomCharacterClassDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertClass(CustomCharacterClassEntity clazz);

    @Update
    void updateClass(CustomCharacterClassEntity clazz);

    @Query("DELETE FROM custom_character_classes WHERE id = :classId")
    void deleteClass(long classId);

    @Transaction
    @Query("SELECT * FROM custom_character_classes ORDER BY name ASC")
    LiveData<List<CustomCharacterClassWithFeatures>> getAllClasses();

    @Query("SELECT * FROM custom_character_classes WHERE game_system = :gameSystem ORDER BY name ASC")
    LiveData<List<CustomCharacterClassEntity>> getClassesByGameSystem(String gameSystem);

    @Query("SELECT * FROM custom_character_classes WHERE game_system = :gameSystem AND (subclass_of IS NULL OR subclass_of = '') ORDER BY name ASC")
    List<CustomCharacterClassEntity> getBaseClassesSync(String gameSystem);

    @Query("SELECT * FROM custom_character_classes WHERE id = :id")
    LiveData<CustomCharacterClassEntity> getClassById(long id);

    @Query("SELECT * FROM custom_character_classes WHERE id = :id")
    CustomCharacterClassEntity getClassByIdSync(long id);

    @Query("SELECT COUNT(*) FROM custom_character_classes WHERE name = :name")
    int countByName(String name);

    @Transaction
    @Query("SELECT * FROM custom_character_classes WHERE id = :classId")
    LiveData<CustomCharacterClassWithFeatures> getClassWithFeatures(long classId);

    @Insert
    void insertFeatures(List<CustomFeatureEntity> features);

    @Query("DELETE FROM custom_features WHERE class_id = :classId")
    void deleteFeaturesForClass(long classId);
}