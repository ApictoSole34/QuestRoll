package com.murkfeatherstudio.questroll.feature_character.managers;

import com.murkfeatherstudio.questroll.core.local_database.Open5eDatabase;
import com.murkfeatherstudio.questroll.core.local_database.PlayerCharacterDatabase;
import com.murkfeatherstudio.questroll.core.models.character.InventoryItemEntity;
import com.murkfeatherstudio.questroll.core.models.open5e.item.ItemEntity;

/**
 * Manager responsible for character inventory operations.
 */
public class InventoryManager {
    private final PlayerCharacterDatabase pcDb;
    private final Open5eDatabase open5eDb;

    public InventoryManager(PlayerCharacterDatabase pcDb, Open5eDatabase open5eDb) {
        this.pcDb = pcDb;
        this.open5eDb = open5eDb;
    }

    public void addItem(InventoryItemEntity item) {
        pcDb.inventoryItemDao().insert(item);
    }

    public void removeItem(long itemId) {
        pcDb.inventoryItemDao().deleteItem(itemId);
    }

    public void equipItem(long itemId) {
        InventoryItemEntity item = pcDb.inventoryItemDao().getById(itemId);
        if (item != null) {
            item.isEquipped = !item.isEquipped;
            pcDb.inventoryItemDao().update(item);
        }
    }

    private float getItemWeightFromOpen5e(String itemKey) {
        if (open5eDb == null || itemKey == null) return 0f;
        ItemEntity item = open5eDb.itemDao().getByKeySync(itemKey);
        return (item != null) ? item.weight : 0f;
    }

    /**
     * Calculates the total weight of all items currently in the character's inventory.
     *
     * @param characterId The unique ID of the character.
     * @return The total weight as a float.
     */
    public float getTotalWeight(long characterId) {
        float total = 0f;
        for (InventoryItemEntity item : pcDb.inventoryItemDao().getByCharacterId(characterId)) {
            float weightPerUnit;
            if (item.itemKey != null && !item.itemKey.isEmpty()) {
                weightPerUnit = getItemWeightFromOpen5e(item.itemKey);
                // If weight from DB is 0, check if there's a custom weight fallback
                if (weightPerUnit == 0f && item.customWeight > 0) {
                    weightPerUnit = item.customWeight;
                }
            } else {
                weightPerUnit = item.customWeight;
            }
            total += weightPerUnit * item.quantity;
        }
        return total;
    }
}
