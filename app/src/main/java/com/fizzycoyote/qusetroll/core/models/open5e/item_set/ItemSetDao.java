package com.fizzycoyote.qusetroll.core.models.open5e.item_set;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface ItemSetDao {
    @Query("SELECT * FROM item_sets")
    List<ItemSetEntity> getAllItemSets();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<ItemSetEntity> itemSets);
}
