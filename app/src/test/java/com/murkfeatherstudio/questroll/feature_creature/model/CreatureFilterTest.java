package com.murkfeatherstudio.questroll.feature_creature.model;

import static org.junit.Assert.*;
import org.junit.Test;

public class CreatureFilterTest {

    @Test
    public void isEmpty_returnsTrueForNewFilter() {
        CreatureFilter filter = new CreatureFilter();
        assertTrue(filter.isEmpty());
    }

    @Test
    public void isEmpty_returnsFalseWhenQuerySet() {
        CreatureFilter filter = new CreatureFilter();
        filter.query = "Goblin";
        assertFalse(filter.isEmpty());
    }

    @Test
    public void isEmpty_returnsFalseWhenCrSet() {
        CreatureFilter filter = new CreatureFilter();
        filter.crMin = 1.0f;
        assertFalse(filter.isEmpty());
        
        filter.crMin = -1f;
        filter.crMax = 5.0f;
        assertFalse(filter.isEmpty());
    }

    @Test
    public void isEmpty_returnsFalseWhenTypeOrAlignmentSet() {
        CreatureFilter filter = new CreatureFilter();
        filter.typeKey = "humanoid";
        assertFalse(filter.isEmpty());

        filter = new CreatureFilter();
        filter.alignment = "Lawful Good";
        assertFalse(filter.isEmpty());
    }

    @Test
    public void reset_logicCheck() {
        CreatureFilter filter = new CreatureFilter();
        filter.query = "Dragon";
        filter.crMin = 10;
        
        // Simulating reset
        filter.query = "";
        filter.crMin = -1f;
        
        assertTrue(filter.isEmpty());
    }
}
