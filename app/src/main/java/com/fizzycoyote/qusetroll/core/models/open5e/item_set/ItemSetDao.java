package com.fizzycoyote.qusetroll.core.models.open5e.item_set;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface ItemSetDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<ItemSetEntity> itemSets);

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
}