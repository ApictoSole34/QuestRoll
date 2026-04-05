package com.fizzycoyote.qusetroll.core.models.open5e.item_category;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface ItemCategoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<ItemCategoryEntity> categories);

    @Query("SELECT COUNT(*) FROM item_categories")
    int getCount();

    @Query("DELETE FROM item_categories")
    void deleteAll();

    @Query("SELECT * FROM item_categories ORDER BY name ASC")
    LiveData<List<ItemCategoryEntity>> getAll();

    @Query("SELECT name FROM item_categories ORDER BY name ASC")
    LiveData<List<String>> getAllNames();

    @Query("SELECT * FROM item_categories WHERE key = :key")
    LiveData<ItemCategoryEntity> getByKey(String key);
}