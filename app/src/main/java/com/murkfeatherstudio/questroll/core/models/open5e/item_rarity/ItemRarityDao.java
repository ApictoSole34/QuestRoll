package com.murkfeatherstudio.questroll.core.models.open5e.item_rarity;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;

import com.murkfeatherstudio.questroll.core.database.base.BaseDao;

import java.util.List;

@Dao
public interface ItemRarityDao extends BaseDao<ItemRarityEntity> {

    @Query("SELECT COUNT(*) FROM item_rarities")
    int getCount();

    @Query("DELETE FROM item_rarities")
    void deleteAll();

    @Query("SELECT * FROM item_rarities ORDER BY rank ASC")
    LiveData<List<ItemRarityEntity>> getAll();

    @Query("SELECT * FROM item_rarities WHERE key = :key")
    LiveData<ItemRarityEntity> getByKey(String key);
}
