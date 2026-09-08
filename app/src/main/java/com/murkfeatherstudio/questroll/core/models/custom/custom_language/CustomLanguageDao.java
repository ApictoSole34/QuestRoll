package com.murkfeatherstudio.questroll.core.models.custom.custom_language;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface CustomLanguageDao {
    @Insert long insert(CustomLanguageEntity language);

    @Update int update(CustomLanguageEntity language);

    @Delete int delete(CustomLanguageEntity language);

    @Query("DELETE FROM custom_languages WHERE id = :id") int deleteById(long id);

    @Query("SELECT * FROM custom_languages") List<CustomLanguageEntity> getAll();

    @Query("SELECT * FROM custom_languages") LiveData<List<CustomLanguageEntity>> getAllLive();

    @Query("SELECT * FROM custom_languages WHERE id = :id LIMIT 1") CustomLanguageEntity findById(long id);

    @Query("SELECT * FROM custom_languages WHERE name = :name LIMIT 1") CustomLanguageEntity findByName(String name);

    @Query("SELECT * FROM custom_languages WHERE script_language = :scriptName")
    List<CustomLanguageEntity> getLanguagesByScript(String scriptName);

    @Query("SELECT COUNT(*) FROM custom_languages WHERE name = :name") int countByName(String name);

    @Query("SELECT * FROM custom_languages WHERE name = :name LIMIT 1") CustomLanguageEntity getByKey(String name);
}
