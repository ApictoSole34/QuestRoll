package com.fizzycoyote.qusetroll.core.models.character;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

/**
 * Data Access Object for {@link InventoryItemEntity}.
 * <p>
 * Manages the character's inventory by handling insertion, deletion, and
 * retrieval of items owned by a specific character.
 * </p>
 */
@Dao
public interface InventoryItemDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(InventoryItemEntity item);

    @Update
    void update(InventoryItemEntity item);

    /**
     * Retrieves all items in a specific character's inventory.
     *
     * @param characterId The ID of the character.
     * @return A list of inventory items.
     */
    @Query("SELECT * FROM inventory_items WHERE character_id = :characterId")
    List<InventoryItemEntity> getByCharacterId(long characterId);

    /**
     * Removes all items from a character's inventory.
     *
     * @param characterId The ID of the character.
     */
    @Query("DELETE FROM inventory_items WHERE character_id = :characterId")
    void deleteForCharacter(long characterId);

    /**
     * Deletes a specific item from the inventory.
     *
     * @param id The unique ID of the inventory item record.
     */
    @Query("DELETE FROM inventory_items WHERE id = :id")
    void deleteItem(long id);

    /**
     * Retrieves a specific inventory item by its record ID.
     */
    @Query("SELECT * FROM inventory_items WHERE id = :id")
    InventoryItemEntity getById(long id);
}
