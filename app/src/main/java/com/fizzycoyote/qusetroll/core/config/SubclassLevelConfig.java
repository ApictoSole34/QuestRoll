package com.fizzycoyote.qusetroll.core.config;

import java.util.LinkedHashMap;
import java.util.Map;

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

    public static boolean needsSubclassAtLevel(String classKey, int classLevel) {
        return getSubclassLevel(classKey) == classLevel;
    }
}