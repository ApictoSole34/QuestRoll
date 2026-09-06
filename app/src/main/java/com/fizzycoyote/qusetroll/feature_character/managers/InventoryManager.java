package com.fizzycoyote.qusetroll.feature_character.managers;

import com.fizzycoyote.qusetroll.core.local_database.PlayerCharacterDatabase;
import com.fizzycoyote.qusetroll.core.models.character.InventoryItemEntity;

/**
 * Manager responsible for character inventory operations.
 * <p>
 * This class handles adding, removing, and equipping items in a character's inventory,
 * as well as calculating the total weight of items carried.
 * </p>
 */
public class InventoryManager {
    private final PlayerCharacterDatabase db;

    public InventoryManager(PlayerCharacterDatabase db) {
        this.db = db;
    }

    /**
     * Adds a new item to the character's inventory.
     *
     * @param item The {@link InventoryItemEntity} to add.
     */
    public void addItem(InventoryItemEntity item) {
        db.inventoryItemDao().insert(item);
    }

    /**
     * Removes an item from the character's inventory by its ID.
     *
     * @param itemId The unique ID of the inventory item to remove.
     */
    public void removeItem(long itemId) {
        db.inventoryItemDao().deleteItem(itemId);
    }

    /**
     * Toggles the equipped state of an item in the inventory.
     *
     * @param itemId The unique ID of the item to equip or unequip.
     */
    public void equipItem(long itemId) {
        InventoryItemEntity item = db.inventoryItemDao().getById(itemId);
        if (item != null) {
            item.isEquipped = !item.isEquipped;
            db.inventoryItemDao().update(item);
        }
    }

    private float getItemWeightFromOpen5e(String itemKey) {
        // TODO: fetch weight from open5e database when needed
        return 0f;
    }

    /**
     * Calculates the total weight of all items currently in the character's inventory.
     *
     * @param characterId The unique ID of the character.
     * @return The total weight as a float.
     */
    public float getTotalWeight(long characterId) {
        float total = 0f;
        for (InventoryItemEntity item : db.inventoryItemDao().getByCharacterId(characterId)) {
            float weightPerUnit = (item.itemKey != null) ? getItemWeightFromOpen5e(item.itemKey) : item.customWeight;
            total += weightPerUnit * item.quantity;
        }
        return total;
    }
}
