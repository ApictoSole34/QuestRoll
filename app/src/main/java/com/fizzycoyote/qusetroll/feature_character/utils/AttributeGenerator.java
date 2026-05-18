package com.fizzycoyote.qusetroll.feature_character.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class AttributeGenerator {

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
     * Roll 4d6, drop the lowest, repeat 6 times.
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
     * Point buy: all attributes start at 8, user spends 27 points.
     */
    public static List<Integer> getPointBuyArray() {
        List<Integer> base = new ArrayList<>();
        for (int i = 0; i < 6; i++) base.add(8);
        return base;
    }

    /**
     * Calculate the cost to raise an attribute from 8 to the given value.
     */
    public static int getPointCost(int value) {
        if (value < 8) return 0;
        if (value <= 13) return value - 8;
        if (value == 14) return 7;
        if (value == 15) return 9;
        return 0; // cannot go above 15
    }
}