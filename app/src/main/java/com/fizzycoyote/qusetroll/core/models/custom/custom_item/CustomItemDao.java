package com.fizzycoyote.qusetroll.core.models.custom.custom_item;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.fizzycoyote.qusetroll.core.models.open5e.item.ItemEntity;

import java.util.List;

@Dao
public interface CustomItemDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(CustomItemEntity item);

    @Update
    void update(CustomItemEntity item);

    @Query("DELETE FROM custom_items WHERE id = :id")
    void delete(long id);

    @Query("SELECT * FROM custom_items ORDER BY name ASC")
    LiveData<List<CustomItemEntity>> getAll();

    @Query("SELECT * FROM custom_items ORDER BY name ASC")
    List<CustomItemEntity> getAllSync();

    @Query("SELECT * FROM custom_items WHERE id = :id")
    LiveData<CustomItemEntity> getById(long id);

    @Query("SELECT * FROM custom_items WHERE id = :id")
    CustomItemEntity getByIdSync(long id);

    @Query("SELECT COUNT(*) FROM custom_items WHERE name = :name")
    int countByName(String name);
}