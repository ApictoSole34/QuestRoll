package com.murkfeatherstudio.questroll.core.models.custom.custom_item_category;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface CustomItemCategoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(CustomItemCategoryEntity category);

    @Update
    void update(CustomItemCategoryEntity category);

    @Query("DELETE FROM custom_item_categories WHERE id = :id")
    void delete(long id);

    @Query("SELECT * FROM custom_item_categories ORDER BY name ASC")
    LiveData<List<CustomItemCategoryEntity>> getAll();

    @Query("SELECT name FROM custom_item_categories ORDER BY name ASC")
    LiveData<List<String>> getAllNames();

    @Query("SELECT * FROM custom_item_categories WHERE id = :id")
    LiveData<CustomItemCategoryEntity> getById(long id);

    @Query("SELECT COUNT(*) FROM custom_item_categories WHERE name = :name")
    int countByName(String name);
}
