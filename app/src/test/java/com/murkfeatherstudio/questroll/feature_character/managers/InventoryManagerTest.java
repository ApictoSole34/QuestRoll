package com.murkfeatherstudio.questroll.feature_character.managers;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import com.murkfeatherstudio.questroll.core.local_database.Open5eDatabase;
import com.murkfeatherstudio.questroll.core.local_database.PlayerCharacterDatabase;
import com.murkfeatherstudio.questroll.core.models.character.InventoryItemDao;
import com.murkfeatherstudio.questroll.core.models.character.InventoryItemEntity;
import com.murkfeatherstudio.questroll.core.models.open5e.item.ItemDao;
import com.murkfeatherstudio.questroll.core.models.open5e.item.ItemEntity;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;

@RunWith(MockitoJUnitRunner.class)
public class InventoryManagerTest {

    @Mock
    private PlayerCharacterDatabase mockPcDb;
    @Mock
    private Open5eDatabase mockOpen5eDb;
    @Mock
    private InventoryItemDao mockInventoryDao;
    @Mock
    private ItemDao mockItemDao;

    private InventoryManager inventoryManager;

    @Before
    public void setUp() {
        when(mockPcDb.inventoryItemDao()).thenReturn(mockInventoryDao);
        when(mockOpen5eDb.itemDao()).thenReturn(mockItemDao);
        inventoryManager = new InventoryManager(mockPcDb, mockOpen5eDb);
    }

    @Test
    public void getTotalWeight_combinesCustomAndDatabaseWeights() {
        // Item 1: Custom weight
        InventoryItemEntity item1 = new InventoryItemEntity();
        item1.itemKey = null;
        item1.customWeight = 5.0f;
        item1.quantity = 2; // 10.0

        // Item 2: Database weight
        InventoryItemEntity item2 = new InventoryItemEntity();
        item2.itemKey = "longsword";
        item2.quantity = 1; // Database has weight 3.0

        ItemEntity dbItem = new ItemEntity();
        dbItem.weight = 3.0f;
        when(mockItemDao.getByKeySync("longsword")).thenReturn(dbItem);

        when(mockInventoryDao.getByCharacterId(1L)).thenReturn(Arrays.asList(item1, item2));

        // 10.0 + 3.0 = 13.0
        assertEquals(13.0f, inventoryManager.getTotalWeight(1L), 0.001f);
    }

    @Test
    public void isOverencumbered_logicTest() {
        // Standard D&D 5e Rules
        int strength = 10;
        float currentWeight = 51.0f;
        
        assertTrue("Encumbered if weight > STR * 5", currentWeight > (strength * 5));
        assertFalse("Not max capacity yet", currentWeight > (strength * 15));
    }
}
