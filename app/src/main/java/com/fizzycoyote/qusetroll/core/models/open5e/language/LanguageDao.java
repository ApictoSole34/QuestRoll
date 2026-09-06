package com.fizzycoyote.qusetroll.core.models.open5e.language;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.fizzycoyote.qusetroll.core.models.custom.custom_language.CustomLanguageEntity;

import java.util.List;

/**
 * Data Access Object for D&D 5e languages (e.g., Common, Elvish) stored in the compendium.
 * <p>
 * Provides access to official language data including their descriptions and scripts.
 * </p>
 */
@Dao
public interface LanguageDao {
    /**
     * Retrieves all languages sorted by name.
     *
     * @return A list of all language entities.
     */
    @Query("SELECT * FROM languages ORDER BY name ASC")
    List<LanguageEntity> getAllLanguages();

    /**
     * Synchronously retrieves all languages.
     *
     * @return A list of all language entities.
     */
    @Query("SELECT * FROM languages")
    List<LanguageEntity> getAllSync();

    /**
     * Retrieves all languages as LiveData.
     *
     * @return LiveData list of language entities.
     */
    @Query("SELECT * FROM languages")
    LiveData<List<LanguageEntity>> getAllLive();


    /**
     * Retrieves a language by its official URL.
     */
    @Query("SELECT * FROM languages WHERE url = :url LIMIT 1")
    LanguageEntity getByUrl(String url);

    /**
     * Retrieves a language by its unique key.
     */
    @Query("SELECT * FROM languages WHERE `key` = :key LIMIT 1")
    LanguageEntity getByKey(String key);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<LanguageEntity> languages);
}
