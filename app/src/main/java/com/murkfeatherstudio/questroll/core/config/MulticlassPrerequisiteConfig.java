package com.murkfeatherstudio.questroll.core.config;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Configuration class defining the ability score prerequisites for multiclassing (D&D 5e).
 * <p>
 * This class implements the validation logic to ensure a character meets the required
 * attribute minimums (typically 13) for both their current classes and the new class
 * they wish to enter.
 * </p>
 */
public final class MulticlassPrerequisiteConfig {

    private static final Map<String, Map<String, Integer>> PREREQS = new LinkedHashMap<>();

    static {
        PREREQS.put("barbarian", Map.of("STR", 13));
        PREREQS.put("bard", Map.of("CHA", 13));
        PREREQS.put("cleric", Map.of("WIS", 13));
        PREREQS.put("druid", Map.of("WIS", 13));
        PREREQS.put("fighter", Map.of("STR", 13, "DEX", 13));
        PREREQS.put("monk", Map.of("DEX", 13, "WIS", 13));
        PREREQS.put("paladin", Map.of("STR", 13, "CHA", 13));
        PREREQS.put("ranger", Map.of("DEX", 13, "WIS", 13));
        PREREQS.put("rogue", Map.of("DEX", 13));
        PREREQS.put("sorcerer", Map.of("CHA", 13));
        PREREQS.put("warlock", Map.of("CHA", 13));
        PREREQS.put("wizard", Map.of("INT", 13));
    }

    private MulticlassPrerequisiteConfig() {}

    /**
     * Checks if a character meets the prerequisites to multiclass into a new class.
     */
    public static boolean meetsMulticlassPrerequisites(List<String> currentClassKeys,
                                                       String newClassKey,
                                                       Map<String, Integer> attributes) {
        return meetsMulticlassPrerequisites(currentClassKeys, newClassKey, attributes, Collections.emptyMap());
    }

    /**
     * Overloaded version that accepts custom class prerequisites.
     *
     * @param currentClassKeys List of keys for classes the character already has.
     * @param newClassKey      The key of the class they wish to add.
     * @param attributes       Map of the character's current ability scores.
     * @param customPrereqs    Map of class keys to their custom prerequisites maps.
     * @return True if all prerequisites are satisfied.
     */
    public static boolean meetsMulticlassPrerequisites(List<String> currentClassKeys,
                                                       String newClassKey,
                                                       Map<String, Integer> attributes,
                                                       Map<String, Map<String, Integer>> customPrereqs) {
        if (attributes == null) return false;

        for (String classKey : currentClassKeys) {
            if (!meetsPrerequisitesForSingleClass(classKey, attributes, customPrereqs)) {
                return false;
            }
        }

        return meetsPrerequisitesForSingleClass(newClassKey, attributes, customPrereqs);
    }

    private static boolean meetsPrerequisitesForSingleClass(String classKey, 
                                                            Map<String, Integer> attributes,
                                                            Map<String, Map<String, Integer>> customPrereqs) {
        if (classKey == null) return true;
        
        Map<String, Integer> required = getRequiredAttributes(classKey, customPrereqs);
        if (required == null || required.isEmpty()) return true;

        String lowerKey = classKey.toLowerCase().trim();
        // Special case for Fighter: STR 13 OR DEX 13
        if (lowerKey.contains("fighter") && required.containsKey("STR") && required.containsKey("DEX")) {
            int str = attributes.getOrDefault("STR", 0);
            int dex = attributes.getOrDefault("DEX", 0);
            return (str >= required.get("STR") || dex >= required.get("DEX"));
        }

        for (Map.Entry<String, Integer> req : required.entrySet()) {
            int current = attributes.getOrDefault(req.getKey(), 0);
            if (current < req.getValue()) {
                return false;
            }
        }
        return true;
    }

    private static Map<String, Integer> getRequiredAttributes(String classKey, Map<String, Map<String, Integer>> customPrereqs) {
        if (classKey != null && classKey.startsWith("custom_")) {
            if (customPrereqs != null && customPrereqs.containsKey(classKey)) {
                return customPrereqs.get(classKey);
            }
            return null;
        }

        String lowerKey = classKey.toLowerCase().trim();
        if (PREREQS.containsKey(lowerKey)) return PREREQS.get(lowerKey);
        
        for (Map.Entry<String, Map<String, Integer>> entry : PREREQS.entrySet()) {
            if (lowerKey.contains(entry.getKey())) {
                return entry.getValue();
            }
        }
        return null;
    }

    public static String getPrerequisiteMessage(String classKey, Map<String, Integer> attributes) {
        return getPrerequisiteMessage(classKey, attributes, Collections.emptyMap());
    }

    public static String getPrerequisiteMessage(String classKey, Map<String, Integer> attributes, Map<String, Map<String, Integer>> customPrereqs) {
        if (classKey == null) return "";

        Map<String, Integer> required = getRequiredAttributes(classKey, customPrereqs);
        if (required == null || required.isEmpty()) return "";

        String lowerKey = classKey.toLowerCase().trim();
        if (lowerKey.contains("fighter") && required.containsKey("STR") && required.containsKey("DEX")) {
            int str = attributes.getOrDefault("STR", 0);
            int dex = attributes.getOrDefault("DEX", 0);
            if (str >= required.get("STR") || dex >= required.get("DEX")) return "";
            return "Requires STR " + required.get("STR") + " or DEX " + required.get("DEX");
        }

        StringBuilder sb = new StringBuilder("Requires: ");
        boolean first = true;
        for (Map.Entry<String, Integer> req : required.entrySet()) {
            int current = attributes.getOrDefault(req.getKey(), 0);
            if (current < req.getValue()) {
                if (!first) sb.append(", ");
                sb.append(req.getKey()).append(" ").append(req.getValue());
                first = false;
            }
        }
        return first ? "" : sb.toString().trim();
    }
}
