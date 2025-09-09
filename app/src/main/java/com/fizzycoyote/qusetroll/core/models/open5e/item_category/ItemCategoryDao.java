package com.fizzycoyote.qusetroll.core.models.open5e.item_category;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface ItemCategoryDao {
    @Query("SELECT * FROM item_categories ORDER BY name ASC")
    List<ItemCategoryEntity> getAllItemCategories();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<ItemCategoryEntity> itemCategories);
}
