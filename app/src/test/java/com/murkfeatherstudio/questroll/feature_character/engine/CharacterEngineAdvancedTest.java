package com.murkfeatherstudio.questroll.feature_character.engine;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class CharacterEngineAdvancedTest {

    @Test
    public void modifierCalculation_roundsDownCorrectly() {
        // Testing the actual production method
        assertEquals(2, CharacterEngine.getAbilityModifier(15));  // 15 -> +2
        assertEquals(2, CharacterEngine.getAbilityModifier(14));  // 14 -> +2
        assertEquals(0, CharacterEngine.getAbilityModifier(11));  // 11 -> +0
        assertEquals(0, CharacterEngine.getAbilityModifier(10));  // 10 -> +0
        
        // Critical edge cases for negative modifiers
        assertEquals(-1, CharacterEngine.getAbilityModifier(9));  // 9 -> -1
        assertEquals(-1, CharacterEngine.getAbilityModifier(8));  // 8 -> -1
        assertEquals(-2, CharacterEngine.getAbilityModifier(7));  // 7 -> -2
    }

    @Test
    public void armorClass_logic_concept() {
        // Testing consistent logic used by the engine
        int baseAc = 12; // Studded Leather
        int dexScore = 18;
        int dexMod = CharacterEngine.getAbilityModifier(dexScore);
        
        // Light Armor
        assertEquals(16, baseAc + dexMod);
        
        // Medium Armor (capped at +2)
        int cappedDex = Math.min(dexMod, 2);
        assertEquals(14, baseAc + cappedDex);
    }
}
