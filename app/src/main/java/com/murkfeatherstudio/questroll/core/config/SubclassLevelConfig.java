package com.murkfeatherstudio.questroll.core.config;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Configuration class that defines at which level each character class chooses its subclass
 * according to D&D 5e rules.
 * <p>
 * For example, Clerics choose at level 1, Wizards at level 2, and Fighters at level 3.
 * </p>
 */
public final class SubclassLevelConfig {

    private static final Map<String, Integer> CLASS_SUBCLASS_LEVEL = new LinkedHashMap<>();

    static {
        CLASS_SUBCLASS_LEVEL.put("cleric",   1);
        CLASS_SUBCLASS_LEVEL.put("sorcerer", 1);
        CLASS_SUBCLASS_LEVEL.put("warlock",  1);

        CLASS_SUBCLASS_LEVEL.put("druid",  2);
        CLASS_SUBCLASS_LEVEL.put("wizard", 2);

        CLASS_SUBCLASS_LEVEL.put("barbarian", 3);
        CLASS_SUBCLASS_LEVEL.put("bard",      3);
        CLASS_SUBCLASS_LEVEL.put("fighter",   3);
        CLASS_SUBCLASS_LEVEL.put("monk",      3);
        CLASS_SUBCLASS_LEVEL.put("paladin",   3);
        CLASS_SUBCLASS_LEVEL.put("ranger",    3);
        CLASS_SUBCLASS_LEVEL.put("rogue",     3);
    }

    private SubclassLevelConfig() {}

    /**
     * Determines the subclass selection level for a given class.
     *
     * @param classKey The unique key of the class.
     * @return The level at which a subclass is selected (defaults to 3).
     */
    public static int getSubclassLevel(String classKey) {
        if (classKey == null || classKey.isEmpty()) return 3;

        if (classKey.startsWith("custom_")) return 3;

        String lower = classKey.toLowerCase().trim();

        if (CLASS_SUBCLASS_LEVEL.containsKey(lower)) {
            return CLASS_SUBCLASS_LEVEL.get(lower);
        }

        for (Map.Entry<String, Integer> entry : CLASS_SUBCLASS_LEVEL.entrySet()) {
            if (lower.contains(entry.getKey())) {
                return entry.getValue();
            }
        }

        return 3;
    }

    /**
     * Checks if a class requires a subclass selection at the specified level.
     *
     * @param classKey   The class identifier.
     * @param classLevel The current level in that class.
     * @return True if a subclass should be chosen at this level.
     */
    public static boolean needsSubclassAtLevel(String classKey, int classLevel) {
        return getSubclassLevel(classKey) == classLevel;
    }
}
