package com.fizzycoyote.qusetroll.feature_character.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Utility class for generating character ability scores (Strength, Dexterity, etc.)
 * using various D&D 5e standard methods.
 */
public class AttributeGenerator {

    /**
     * Provides the Standard Array of ability scores: 15, 14, 13, 12, 10, 8.
     *
     * @return A list of integers representing the standard array, sorted descending.
     */
    public static List<Integer> getStandardArray() {
        List<Integer> stats = new ArrayList<>();
        stats.add(15);
        stats.add(14);
        stats.add(13);
        stats.add(12);
        stats.add(10);
        stats.add(8);
        Collections.sort(stats, Collections.reverseOrder());
        return stats;
    }

    /**
     * Generates six ability scores by rolling 4d6 and dropping the lowest die for each score.
     *
     * @return A list of six integers, sorted descending.
     */
    public static List<Integer> roll4d6DropLowest() {
        List<Integer> results = new ArrayList<>();
        Random rand = new Random();
        for (int i = 0; i < 6; i++) {
            int[] rolls = {rand.nextInt(6) + 1, rand.nextInt(6) + 1, rand.nextInt(6) + 1, rand.nextInt(6) + 1};
            int min = Integer.MAX_VALUE;
            int sum = 0;
            for (int r : rolls) {
                sum += r;
                if (r < min) min = r;
            }
            results.add(sum - min);
        }
        Collections.sort(results, Collections.reverseOrder());
        return results;
    }

    /**
     * Provides the starting array for the Point Buy method (all 8s).
     *
     * @return A list of six integers, all initialized to 8.
     */
    public static List<Integer> getPointBuyArray() {
        List<Integer> base = new ArrayList<>();
        for (int i = 0; i < 6; i++) base.add(8);
        return base;
    }

    /**
     * Calculates the point cost to raise an attribute score from 8 to the specified value.
     * <p>
     * Costs follow standard 5e rules: 8-13 is 1 point per increase, 14 and 15 cost 2 points each.
     * </p>
     *
     * @param value The desired score (8-15).
     * @return The total point cost.
     */
    public static int getPointCost(int value) {
        if (value < 8) return 0;
        if (value <= 13) return value - 8;
        if (value == 14) return 7;
        if (value == 15) return 9;
        return 0; // cannot go above 15
    }
}
