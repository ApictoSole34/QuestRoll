package com.murkfeatherstudio.questroll.feature_character.utils;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class BonusParserTest {

    @Test
    public void parseAbilityBonuses_extractsIncreasesFromText() {
        String traitsJson = "[{\"name\": \"Ability Score Increase\", \"desc\": \"Your Constitution score increases by 2 and your Charisma score increases by 1.\"}]";
        int[] bonuses = BonusParser.parseAbilityBonusesFromTraits(traitsJson);

        // Indices: 0:STR, 1:DEX, 2:CON, 3:INT, 4:WIS, 5:CHA
        assertEquals(0, bonuses[0]);
        assertEquals(0, bonuses[1]);
        assertEquals(2, bonuses[2]);
        assertEquals(0, bonuses[3]);
        assertEquals(0, bonuses[4]);
        assertEquals(1, bonuses[5]);
    }

    @Test
    public void parseAbilityBonuses_handlesHumanSRD() {
        String traitsJson = "[{\"name\": \"Ability Score Increase\", \"desc\": \"Your ability scores each increase by 1.\"}]";
        int[] bonuses = BonusParser.parseAbilityBonusesFromTraits(traitsJson);

        for (int i = 0; i < 6; i++) {
            assertEquals("Score at index " + i + " should be 1", 1, bonuses[i]);
        }
    }

    @Test
    public void parseAbilityBonuses_extractsPlusFormat() {
        String traitsJson = "[{\"name\": \"Ability Score Increase\", \"desc\": \"+2 to Strength, +1 to Dexterity\"}]";
        int[] bonuses = BonusParser.parseAbilityBonusesFromTraits(traitsJson);

        assertEquals(2, bonuses[0]);
        assertEquals(1, bonuses[1]);
        assertEquals(0, bonuses[2]);
    }

    @Test
    public void parseAbilityBonuses_handlesEmptyOrNull() {
        int[] bonuses = BonusParser.parseAbilityBonusesFromTraits(null);
        for (int b : bonuses) assertEquals(0, b);

        bonuses = BonusParser.parseAbilityBonusesFromTraits("");
        for (int b : bonuses) assertEquals(0, b);
    }
}
