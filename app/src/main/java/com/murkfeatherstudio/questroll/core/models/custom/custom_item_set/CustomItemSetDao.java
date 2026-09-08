package com.murkfeatherstudio.questroll.core.models.custom.custom_item_set;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface CustomItemSetDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(CustomItemSetEntity itemSet);

    @Update
    void update(CustomItemSetEntity itemSet);

    @Query("DELETE FROM custom_item_sets WHERE id = :id")
    void delete(long id);

    @Query("SELECT * FROM custom_item_sets ORDER BY name ASC")
    List<CustomItemSetEntity> getAllSync();

    @Query("SELECT * FROM custom_item_sets ORDER BY name ASC")
    LiveData<List<CustomItemSetEntity>> getAll();

    @Query("SELECT * FROM custom_item_sets WHERE id = :id")
    LiveData<CustomItemSetEntity> getById(long id);

    @Query("SELECT COUNT(*) FROM custom_item_sets WHERE name = :name")
    int countByName(String name);
}
