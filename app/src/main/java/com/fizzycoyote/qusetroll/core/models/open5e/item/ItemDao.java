package com.fizzycoyote.qusetroll.core.models.open5e.item;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface ItemDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<ItemEntity> items);

    @Query("SELECT COUNT(*) FROM items")
    int getCount();

    @Query("DELETE FROM items")
    void deleteAll();

    @Query("SELECT * FROM items WHERE " +
            "(:query = '' OR name LIKE '%' || :query || '%') AND " +
            "(:categoryName = '' OR category_name = :categoryName) AND " +
            "(:source = '' OR document_name = :source) AND " +
            "(:magicOnly = 0 OR is_magic_item = 1) AND " +
            "(:rarity = '' OR rarity_name = :rarity) AND " +
            "(:attunement = 0 OR requires_attunement = 1) " +
            "ORDER BY name ASC")
    LiveData<List<ItemEntity>> getFiltered(String query, String categoryName, String source,
                                           int magicOnly, String rarity, int attunement);

    @Query("SELECT * FROM items WHERE key = :key")
    LiveData<ItemEntity> getByKey(String key);

    @Query("SELECT DISTINCT document_name FROM items WHERE document_name IS NOT NULL ORDER BY document_name ASC")
    List<String> getDistinctSources();

    @Query("SELECT DISTINCT category_name FROM items ORDER BY category_name ASC")
    List<String> getDistinctCategories();
}