package com.murkfeatherstudio.questroll.feature_character.utils;

import static org.junit.Assert.*;
import org.junit.Test;

public class ClassCastingAbilityTest {

    @Test
    public void getCastingAbilityForClass_returnsCorrectAbility() {
        assertEquals("CHA", ClassCastingAbility.getCastingAbilityForClass("Bard"));
        assertEquals("WIS", ClassCastingAbility.getCastingAbilityForClass("Cleric"));
        assertEquals("INT", ClassCastingAbility.getCastingAbilityForClass("Wizard"));
        assertEquals("CHA", ClassCastingAbility.getCastingAbilityForClass("Warlock"));
    }

    @Test
    public void getCastingAbilityForClass_returnsNullForNonCasters() {
        assertNull(ClassCastingAbility.getCastingAbilityForClass("Barbarian"));
        assertNull(ClassCastingAbility.getCastingAbilityForClass("Fighter"));
    }

    @Test
    public void getCastingAbilityForClass_isCaseInsensitive() {
        assertEquals("WIS", ClassCastingAbility.getCastingAbilityForClass("druid"));
        assertEquals("CHA", ClassCastingAbility.getCastingAbilityForClass("SORCERER"));
    }

    @Test
    public void getCastingAbilityForClass_returnsDefaultIntForUnknown() {
        assertEquals("INT", ClassCastingAbility.getCastingAbilityForClass("UnknownClass"));
    }
}
