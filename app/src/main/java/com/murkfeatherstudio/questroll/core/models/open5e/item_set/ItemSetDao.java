package com.murkfeatherstudio.questroll.core.models.open5e.item_set;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;

import com.murkfeatherstudio.questroll.core.database.base.BaseDao;

import java.util.List;

@Dao
public interface ItemSetDao extends BaseDao<ItemSetEntity> {

    @Query("SELECT COUNT(*) FROM item_sets")
    int getCount();

    @Query("DELETE FROM item_sets")
    void deleteAll();

    @Query("SELECT * FROM item_sets")
    List<ItemSetEntity> getAllSync();

    @Query("SELECT * FROM item_sets ORDER BY name ASC")
    LiveData<List<ItemSetEntity>> getAll();

    @Query("SELECT * FROM item_sets WHERE key = :key")
    LiveData<ItemSetEntity> getByKey(String key);

    @Query("SELECT DISTINCT document_url FROM item_sets WHERE document_url IS NOT NULL ORDER BY document_url ASC")
    LiveData<List<String>> getDistinctSources();

    @Query("SELECT iset.* FROM item_sets iset " +
            "INNER JOIN documents d ON iset.document_url = d.url " +
            "WHERE d.gamesystem = :gameSystem " +
            "ORDER BY iset.name ASC")
    List<ItemSetEntity> getAllByGameSystem(String gameSystem);
}
