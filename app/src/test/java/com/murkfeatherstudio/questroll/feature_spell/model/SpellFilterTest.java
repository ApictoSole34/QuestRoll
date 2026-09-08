package com.murkfeatherstudio.questroll.feature_spell.model;

import static org.junit.Assert.*;
import org.junit.Test;

public class SpellFilterTest {

    @Test
    public void isEmpty_returnsTrueForNewFilter() {
        SpellFilter filter = new SpellFilter();
        assertTrue(filter.isEmpty());
    }

    @Test
    public void isEmpty_returnsFalseWhenQuerySet() {
        SpellFilter filter = new SpellFilter();
        filter.query = "Fireball";
        assertFalse(filter.isEmpty());
    }

    @Test
    public void isEmpty_returnsFalseWhenLevelSet() {
        SpellFilter filter = new SpellFilter();
        filter.level = 3;
        assertFalse(filter.isEmpty());
    }

    @Test
    public void isEmpty_returnsFalseWhenRitualOnlySet() {
        SpellFilter filter = new SpellFilter();
        filter.ritualOnly = true;
        assertFalse(filter.isEmpty());
    }

    @Test
    public void reset_worksCorrectly() {
        SpellFilter filter = new SpellFilter();
        filter.query = "Healing";
        filter.level = 1;
        filter.ritualOnly = true;
        
        // Manual reset to check isEmpty logic
        filter.query = "";
        filter.level = -1;
        filter.ritualOnly = false;
        
        assertTrue(filter.isEmpty());
    }
}
