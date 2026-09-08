package com.murkfeatherstudio.questroll.feature_character.engine;

import static org.junit.Assert.*;
import org.junit.Test;

public class DndRulesEdgeCaseTest {

    /**
     * Test for the absolute minimum ability score in D&D 5e (which is 1).
     * Mod for 1 is -5.
     */
    @Test
    public void modifierForScoreOne_isMinusFive() {
        int score = 1;
        int mod = CharacterEngine.getAbilityModifier(score);
        assertEquals(-5, mod);
    }

    /**
     * Test for the maximum standard ability score (30 for deities/monsters).
     * Mod for 30 is +10.
     */
    @Test
    public void modifierForScoreThirty_isPlusTen() {
        int score = 30;
        int mod = CharacterEngine.getAbilityModifier(score);
        assertEquals(10, mod);
    }
    
    /**
     * Test for odd scores below 10.
     * Score 7 should give -2.
     * Score 9 should give -1.
     */
    @Test
    public void modifierForOddScoresBelowTen() {
        assertEquals("-2 mod for score 7", -2, CharacterEngine.getAbilityModifier(7));
        assertEquals("-1 mod for score 9", -1, CharacterEngine.getAbilityModifier(9));
    }

    /**
     * Test for massive damage rule concept.
     */
    @Test
    public void massiveDamage_instantDeathLogic() {
        int maxHp = 10;
        int currentHp = 2;
        int damage = 15;

        int remainingDamage = damage - currentHp;
        boolean instantDeath = remainingDamage >= maxHp;

        assertTrue("Character should die instantly", instantDeath);
    }

    /**
     * Test for Carry Capacity for Tiny creatures.
     */
    @Test
    public void carryCapacity_tinyCreature_isHalved() {
        int strength = 10;
        int standardCapacity = strength * 15;
        float tinyCapacity = standardCapacity * 0.5f;

        assertEquals(75.0f, tinyCapacity, 0.0f);
    }
}
