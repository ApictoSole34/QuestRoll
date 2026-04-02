package com.fizzycoyote.qusetroll.core.models.open5e.item_rarity;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface ItemRarityDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<ItemRarityEntity> rarities);

    @Query("SELECT COUNT(*) FROM item_rarities")
    int getCount();

    @Query("DELETE FROM item_rarities")
    void deleteAll();

    @Query("SELECT * FROM item_rarities ORDER BY rank ASC")
    LiveData<List<ItemRarityEntity>> getAll();

    @Query("SELECT * FROM item_rarities WHERE key = :key")
    LiveData<ItemRarityEntity> getByKey(String key);
}