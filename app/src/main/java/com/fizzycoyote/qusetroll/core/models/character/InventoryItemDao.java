package com.fizzycoyote.qusetroll.core.models.character;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface InventoryItemDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(InventoryItemEntity item);

    @Update
    void update(InventoryItemEntity item);

    @Query("SELECT * FROM inventory_items WHERE character_id = :characterId")
    List<InventoryItemEntity> getByCharacterId(long characterId);

    @Query("DELETE FROM inventory_items WHERE character_id = :characterId")
    void deleteForCharacter(long characterId);

    @Query("DELETE FROM inventory_items WHERE id = :id")
    void deleteItem(long id);

    @Query("SELECT * FROM inventory_items WHERE id = :id")
    InventoryItemEntity getById(long id);
}