package com.fizzycoyote.qusetroll.core.models.open5e.language;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.fizzycoyote.qusetroll.core.models.custom.custom_language.CustomLanguageEntity;

import java.util.List;

@Dao
public interface LanguageDao {
    @Query("SELECT * FROM languages ORDER BY name ASC")
    List<LanguageEntity> getAllLanguages();

    @Query("SELECT * FROM languages")
    List<LanguageEntity> getAllSync();

    @Query("SELECT * FROM languages")
    LiveData<List<LanguageEntity>> getAllLive();


    @Query("SELECT * FROM languages WHERE url = :url LIMIT 1")
    LanguageEntity getByUrl(String url);

    @Query("SELECT * FROM languages WHERE `key` = :key LIMIT 1")
    LanguageEntity getByKey(String key);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<LanguageEntity> languages);
}
