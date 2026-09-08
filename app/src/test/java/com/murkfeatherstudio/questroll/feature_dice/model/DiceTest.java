package com.murkfeatherstudio.questroll.feature_dice.model;

import static org.junit.Assert.*;
import org.junit.Test;

public class DiceTest {

    @Test
    public void roll_resultIsWithinBounds() {
        Dice d20 = new Dice(20);
        for (int i = 0; i < 100; i++) {
            int result = d20.roll();
            assertTrue("Result " + result + " should be >= 1", result >= 1);
            assertTrue("Result " + result + " should be <= 20", result <= 20);
        }
    }

    @Test
    public void getGifName_returnsCorrectFormat() {
        Dice d6 = new Dice(6);
        // We need to set the result or roll it.
        // Since roll() is random, we check if the format matches after a roll.
        int res = d6.roll();
        String expected = "d6s" + res;
        assertEquals(expected, d6.getGifName());
    }

    @Test
    public void isRolled_isFalseInitially_andTrueAfterRoll() {
        Dice d10 = new Dice(10);
        assertFalse(d10.isRolled());
        d10.roll();
        assertTrue(d10.isRolled());
    }

    @Test
    public void animationState_resetOnRoll() {
        Dice d8 = new Dice(8);
        d8.setAnimationPlayed(true);
        d8.roll();
        assertFalse(d8.isAnimationPlayed());
    }
}
