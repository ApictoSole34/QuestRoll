package com.murkfeatherstudio.questroll.feature_character.utils;

import static org.junit.Assert.*;
import org.junit.Test;
import java.util.List;

public class AttributeGeneratorTest {

    @Test
    public void getStandardArray_returnsCorrectValues() {
        List<Integer> stats = AttributeGenerator.getStandardArray();
        assertEquals(6, stats.size());
        assertEquals(Integer.valueOf(15), stats.get(0));
        assertEquals(Integer.valueOf(14), stats.get(1));
        assertEquals(Integer.valueOf(13), stats.get(2));
        assertEquals(Integer.valueOf(12), stats.get(3));
        assertEquals(Integer.valueOf(10), stats.get(4));
        assertEquals(Integer.valueOf(8), stats.get(5));
    }

    @Test
    public void roll4d6DropLowest_returnsSixValuesInValidRange() {
        List<Integer> stats = AttributeGenerator.roll4d6DropLowest();
        assertEquals(6, stats.size());
        for (int s : stats) {
            assertTrue("Stat " + s + " should be >= 3", s >= 3);
            assertTrue("Stat " + s + " should be <= 18", s <= 18);
        }
    }

    @Test
    public void getPointBuyArray_returnsAllEights() {
        List<Integer> stats = AttributeGenerator.getPointBuyArray();
        assertEquals(6, stats.size());
        for (int s : stats) {
            assertEquals(8, s);
        }
    }

    @Test
    public void getPointCost_returnsCorrectCostsFor5e() {
        assertEquals(0, AttributeGenerator.getPointCost(8));
        assertEquals(1, AttributeGenerator.getPointCost(9));
        assertEquals(5, AttributeGenerator.getPointCost(13));
        assertEquals(7, AttributeGenerator.getPointCost(14));
        assertEquals(9, AttributeGenerator.getPointCost(15));
        assertEquals(0, AttributeGenerator.getPointCost(16)); // Max is 15 in point buy
    }
}
