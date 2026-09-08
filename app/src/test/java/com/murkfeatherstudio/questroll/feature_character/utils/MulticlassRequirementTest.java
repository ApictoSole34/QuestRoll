package com.murkfeatherstudio.questroll.feature_character.utils;

import static org.junit.Assert.*;
import org.junit.Test;
import java.util.HashMap;
import java.util.Map;

public class MulticlassRequirementTest {

    @Test
    public void isSatisfied_primaryOnly_returnsCorrectResult() {
        MulticlassRequirement wizardReq = MulticlassRequirement.WIZARD; // INT 13
        Map<String, Integer> attrs = new HashMap<>();
        
        attrs.put("INT", 12);
        assertFalse("Wizard should require 13 INT", wizardReq.isSatisfied(attrs));
        
        attrs.put("INT", 13);
        assertTrue("Wizard with 13 INT should satisfy requirement", wizardReq.isSatisfied(attrs));
    }

    @Test
    public void isSatisfied_bothRequired_returnsCorrectResult() {
        MulticlassRequirement paladinReq = MulticlassRequirement.PALADIN; // STR 13 AND CHA 13
        Map<String, Integer> attrs = new HashMap<>();
        
        attrs.put("STR", 13);
        attrs.put("CHA", 12);
        assertFalse("Paladin should require both STR and CHA 13", paladinReq.isSatisfied(attrs));
        
        attrs.put("CHA", 13);
        assertTrue("Paladin with both 13 should satisfy requirement", paladinReq.isSatisfied(attrs));
    }

    @Test
    public void isSatisfied_eitherRequired_returnsCorrectResult() {
        MulticlassRequirement fighterReq = MulticlassRequirement.FIGHTER; // STR 13 OR DEX 13
        Map<String, Integer> attrs = new HashMap<>();
        
        attrs.put("STR", 12);
        attrs.put("DEX", 12);
        assertFalse("Fighter requires at least one of STR or DEX 13", fighterReq.isSatisfied(attrs));
        
        attrs.put("STR", 13);
        assertTrue("Fighter with 13 STR should satisfy requirement", fighterReq.isSatisfied(attrs));
        
        attrs.put("STR", 12);
        attrs.put("DEX", 13);
        assertTrue("Fighter with 13 DEX should satisfy requirement", fighterReq.isSatisfied(attrs));
    }

    @Test
    public void fromClassName_isCaseInsensitive() {
        assertEquals(MulticlassRequirement.BARBARIAN, MulticlassRequirement.fromClassName("barbarian"));
        assertEquals(MulticlassRequirement.BARBARIAN, MulticlassRequirement.fromClassName("BARBARIAN"));
        assertNull(MulticlassRequirement.fromClassName("NonExistentClass"));
    }
}
