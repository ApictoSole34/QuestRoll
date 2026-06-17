package com.fizzycoyote.qusetroll.core.config;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

    public static boolean meetsMulticlassPrerequisites(List<String> currentClassKeys,
                                                       String newClassKey,
                                                       Map<String, Integer> attributes) {
        if (attributes == null) return false;

        for (String classKey : currentClassKeys) {
            if (!meetsPrerequisitesForSingleClass(classKey, attributes)) {
                return false;
            }
        }

        if (!meetsPrerequisitesForSingleClass(newClassKey, attributes)) {
            return false;
        }

        return true;
    }

    private static boolean meetsPrerequisitesForSingleClass(String classKey, Map<String, Integer> attributes) {
        if (classKey == null) return true;
        if (classKey.startsWith("custom_")) return true; // custom klasy – brak wymagań lub można dodać osobno

        String lowerKey = classKey.toLowerCase().trim();
        Map<String, Integer> required = null;

        if (PREREQS.containsKey(lowerKey)) {
            required = PREREQS.get(lowerKey);
        } else {
            for (Map.Entry<String, Map<String, Integer>> entry : PREREQS.entrySet()) {
                if (lowerKey.contains(entry.getKey())) {
                    required = entry.getValue();
                    break;
                }
            }
        }

        if (required == null) return true;

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

    public static String getPrerequisiteMessage(String classKey, Map<String, Integer> attributes) {
        if (classKey == null) return "";
        if (classKey.startsWith("custom_")) return "";

        String lowerKey = classKey.toLowerCase().trim();
        Map<String, Integer> required = null;

        if (PREREQS.containsKey(lowerKey)) {
            required = PREREQS.get(lowerKey);
        } else {
            for (Map.Entry<String, Map<String, Integer>> entry : PREREQS.entrySet()) {
                if (lowerKey.contains(entry.getKey())) {
                    required = entry.getValue();
                    break;
                }
            }
        }
        if (required == null) return "";

        if (lowerKey.contains("fighter") && required.containsKey("STR") && required.containsKey("DEX")) {
            int str = attributes.getOrDefault("STR", 0);
            int dex = attributes.getOrDefault("DEX", 0);
            if (str >= required.get("STR") || dex >= required.get("DEX")) return "";
            return "Requires STR " + required.get("STR") + " or DEX " + required.get("DEX");
        }

        StringBuilder sb = new StringBuilder("Requires: ");
        for (Map.Entry<String, Integer> req : required.entrySet()) {
            int current = attributes.getOrDefault(req.getKey(), 0);
            if (current < req.getValue()) {
                sb.append(req.getKey()).append(" ").append(req.getValue()).append("  ");
            }
        }
        return sb.toString().trim();
    }
}