package com.murkfeatherstudio.questroll.core.models.custom.custom_character_class.custom_class_progression;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface CustomClassProgressionDao {
    @Query("SELECT * FROM custom_class_progression WHERE class_key = :classKey ORDER BY level")
    LiveData<List<CustomClassProgressionEntity>> getProgressionForClass(String classKey);

    @Query("SELECT * FROM custom_class_progression WHERE class_key = :classKey AND level = :level")
    LiveData<CustomClassProgressionEntity> getProgressionForClassAtLevel(String classKey, int level);

    @Insert
    void insert(CustomClassProgressionEntity progression);

    @Update
    void update(CustomClassProgressionEntity progression);
}