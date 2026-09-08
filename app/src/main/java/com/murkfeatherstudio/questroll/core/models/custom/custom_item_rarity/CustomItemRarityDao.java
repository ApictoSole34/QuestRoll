package com.murkfeatherstudio.questroll.core.models.custom.custom_item_rarity;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface CustomItemRarityDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(CustomItemRarityEntity rarity);

    @Update
    void update(CustomItemRarityEntity rarity);

    @Query("DELETE FROM custom_item_rarities WHERE id = :id")
    void delete(long id);

    @Query("SELECT * FROM custom_item_rarities ORDER BY rank ASC, name ASC")
    LiveData<List<CustomItemRarityEntity>> getAll();

    @Query("SELECT * FROM custom_item_rarities WHERE id = :id")
    LiveData<CustomItemRarityEntity> getById(long id);

    @Query("SELECT * FROM custom_item_rarities WHERE id = :id")
    CustomItemRarityEntity getByIdSync(long id);

    @Query("SELECT COUNT(*) FROM custom_item_rarities WHERE name = :name")
    int countByName(String name);
}