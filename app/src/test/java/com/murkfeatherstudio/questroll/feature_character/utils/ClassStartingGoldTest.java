package com.murkfeatherstudio.questroll.feature_character.utils;

import static org.junit.Assert.*;
import org.junit.Test;

public class ClassStartingGoldTest {

    @Test
    public void fromClassName_returnsCorrectEnum() {
        assertEquals(ClassStartingGold.WIZARD, ClassStartingGold.fromClassName("Wizard"));
        assertEquals(ClassStartingGold.BARBARIAN, ClassStartingGold.fromClassName("barbarian"));
        // Fallback check
        assertEquals(ClassStartingGold.FIGHTER, ClassStartingGold.fromClassName("UnknownClass"));
    }

    @Test
    public void rollGold_returnsValueWithinPossibleRange() {
        ClassStartingGold wizardGold = ClassStartingGold.WIZARD; // 4d4 * 10
        int min = 4 * 1 * 10;
        int max = 4 * 4 * 10;
        
        for (int i = 0; i < 100; i++) {
            int result = wizardGold.rollGold();
            assertTrue("Gold " + result + " should be >= " + min, result >= min);
            assertTrue("Gold " + result + " should be <= " + max, result <= max);
            assertEquals("Gold " + result + " should be multiple of 10", 0, result % 10);
        }
    }

    @Test
    public void getDescription_returnsCorrectFormat() {
        assertEquals("5d4 × 10", ClassStartingGold.FIGHTER.getDescription());
        assertEquals("2d4 × 10", ClassStartingGold.DRUID.getDescription());
    }
}
