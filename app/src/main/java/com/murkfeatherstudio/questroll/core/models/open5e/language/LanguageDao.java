package com.murkfeatherstudio.questroll.core.models.open5e.language;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;

import com.murkfeatherstudio.questroll.core.database.base.BaseDao;

import java.util.List;

/**
 * Data Access Object for D&D 5e languages (e.g., Common, Elvish) stored in the compendium.
 */
@Dao
public interface LanguageDao extends BaseDao<LanguageEntity> {
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
}
