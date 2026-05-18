package com.fizzycoyote.qusetroll.feature_character.managers;

import com.fizzycoyote.qusetroll.core.local_database.PlayerCharacterDatabase;
import com.fizzycoyote.qusetroll.core.models.character.InventoryItemEntity;

public class InventoryManager {
    private final PlayerCharacterDatabase db;

    public InventoryManager(PlayerCharacterDatabase db) {
        this.db = db;
    }

    public void addItem(InventoryItemEntity item) {
        db.inventoryItemDao().insert(item);
    }

    public void removeItem(long itemId) {
        db.inventoryItemDao().deleteItem(itemId);
    }

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

    public float getTotalWeight(long characterId) {
        float total = 0f;
        for (InventoryItemEntity item : db.inventoryItemDao().getByCharacterId(characterId)) {
            float weightPerUnit = (item.itemKey != null) ? getItemWeightFromOpen5e(item.itemKey) : item.customWeight;
            total += weightPerUnit * item.quantity;
        }
        return total;
    }
}
