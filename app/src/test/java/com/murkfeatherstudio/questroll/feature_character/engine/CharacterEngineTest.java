package com.murkfeatherstudio.questroll.feature_character.engine;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

public class CharacterEngineTest {

    private CharacterEngine engine;

    @Before
    public void setUp() {
        // Używamy konstruktora DI z nullami, aby uniknąć wywołania Room.databaseBuilder,
        // ponieważ testujemy tu tylko metody, które nie korzystają bezpośrednio z bazy danych w tych testach.
        engine = new CharacterEngine(null, null);
    }

    @Test
    public void getProficiencyBonus_calculatesCorrectValues() {
        // Testowanie metody statycznej
        assertEquals(2, CharacterEngine.getProficiencyBonus(1));
        assertEquals(2, CharacterEngine.getProficiencyBonus(4));
        assertEquals(3, CharacterEngine.getProficiencyBonus(5));
        assertEquals(4, CharacterEngine.getProficiencyBonus(9));
        assertEquals(5, CharacterEngine.getProficiencyBonus(13));
        assertEquals(6, CharacterEngine.getProficiencyBonus(17));
        assertEquals(6, CharacterEngine.getProficiencyBonus(20));
    }

    @Test
    public void getAbilityModifier_calculatesCorrectValues_includingNegatives() {
        // Testowanie logiki modyfikatorów
        assertEquals(-5, CharacterEngine.getAbilityModifier(1));
        assertEquals(-4, CharacterEngine.getAbilityModifier(3));
        assertEquals(-2, CharacterEngine.getAbilityModifier(7));
        assertEquals(-1, CharacterEngine.getAbilityModifier(8));
        assertEquals(-1, CharacterEngine.getAbilityModifier(9));
        assertEquals(0, CharacterEngine.getAbilityModifier(10));
        assertEquals(0, CharacterEngine.getAbilityModifier(11));
        assertEquals(1, CharacterEngine.getAbilityModifier(12));
        assertEquals(2, CharacterEngine.getAbilityModifier(15));
        assertEquals(5, CharacterEngine.getAbilityModifier(20));
    }

    @Test
    public void hpParsing_handlesStandardAndComplex5eFormats() {
        // Testowanie parsowania HP
        assertEquals(10, CharacterEngine.parseHpString("8 + 2", 2, true));
        assertEquals(7, CharacterEngine.parseHpString("1d8 (or 5) + 2", 2, false));
        assertEquals(5, CharacterEngine.parseHpString("1d8", 0, false));
        assertEquals(8, CharacterEngine.parseHpString("1d8", 0, true));
        assertEquals(3, CharacterEngine.parseHpString("6", -3, true));
    }
}
