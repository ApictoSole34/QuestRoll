package com.murkfeatherstudio.questroll.core.models.open5e.background;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;

import com.murkfeatherstudio.questroll.core.database.base.BaseDao;

import java.util.List;

/**
 * Data Access Object for D&D 5e backgrounds (e.g., Acolyte, Criminal) stored in the compendium.
 */
@Dao
public interface BackgroundDao extends BaseDao<BackgroundEntity> {

    @Query("SELECT COUNT(*) FROM backgrounds")
    int getCount();

    @Query("DELETE FROM backgrounds")
    void deleteAll();

    /**
     * Retrieves all backgrounds associated with a specific game system.
     */
    @Query("SELECT b.* FROM backgrounds b " +
            "INNER JOIN documents d ON b.document_key = d.key " +
            "WHERE d.gamesystem = :gameSystem " +
            "ORDER BY b.name ASC")
    List<BackgroundEntity> getByGameSystem(String gameSystem);

    /**
     * Searches and filters backgrounds by name and source document.
     */
    @Query("SELECT * FROM backgrounds WHERE " +
            "(:query = '' OR name LIKE '%' || :query || '%') AND " +
            "(:source = '' OR document_name LIKE '%' || :source || '%') " +
            "ORDER BY name ASC")
    LiveData<List<BackgroundEntity>> getFiltered(String query, String source);

    /**
     * Retrieves a specific background by its unique key.
     */
    @Query("SELECT * FROM backgrounds WHERE key = :key")
    LiveData<BackgroundEntity> getByKey(String key);

    /**
     * Retrieves a list of unique source document names present in the background database.
     */
    @Query("SELECT DISTINCT document_name FROM backgrounds WHERE document_name IS NOT NULL ORDER BY document_name ASC")
    List<String> getDistinctSources();

    @Query("SELECT * FROM backgrounds")
    List<BackgroundEntity> getAll();

    @Query("SELECT * FROM backgrounds WHERE key = :key")
    BackgroundEntity getByKeySync(String key);
}
