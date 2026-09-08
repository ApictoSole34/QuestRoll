package com.murkfeatherstudio.questroll.feature_character.engine;

import static org.junit.Assert.*;
import org.junit.Test;

public class CharacterMagicAndLevelTest {

    /**
     * Test for the XP to Level calculation logic using actual production code.
     */
    @Test
    public void xpToLevel_returnsCorrectLevel() {
        assertEquals(1, CharacterEngine.getLevelFromXp(0));
        assertEquals(1, CharacterEngine.getLevelFromXp(299));
        assertEquals(2, CharacterEngine.getLevelFromXp(300));
        assertEquals(3, CharacterEngine.getLevelFromXp(900));
        assertEquals(4, CharacterEngine.getLevelFromXp(2700));
        assertEquals(5, CharacterEngine.getLevelFromXp(6500));
        assertEquals(20, CharacterEngine.getLevelFromXp(355000));
        assertEquals(20, CharacterEngine.getLevelFromXp(1000000));
    }

    /**
     * Test for spell save DC calculation using production logic.
     */
    @Test
    public void spellSaveDC_isCorrect() {
        int proficiency = 2; // Level 1
        int wisdomMod = 3;   // Wis 16
        
        int saveDc = CharacterEngine.calculateSpellSaveDc(proficiency, wisdomMod);
        assertEquals(13, saveDc);
    }

    /**
     * Test for spell attack bonus using production logic.
     */
    @Test
    public void spellAttackBonus_isCorrect() {
        int proficiency = 3; // Level 5
        int charismaMod = 4; // Cha 18
        
        int attackBonus = CharacterEngine.calculateSpellAttackBonus(proficiency, charismaMod);
        assertEquals(7, attackBonus);
    }
}
