package com.murkfeatherstudio.questroll.feature_character.utils;

import java.util.Map;

/**
 * Defines the attribute requirements for multiclassing into different classes (D&D 5e).
 * <p>
 * In 5e, to multiclass into a new class, a character must meet certain ability score
 * minimums (typically 13). This enum encapsulates those requirements and provides
 * logic to check them against a character's current attributes.
 * </p>
 */
public enum MulticlassRequirement {
    BARBARIAN("Barbarian", "STR", 13, null, 0, RequirementType.PRIMARY_ONLY),
    BARD("Bard", "CHA", 13, null, 0, RequirementType.PRIMARY_ONLY),
    CLERIC("Cleric", "WIS", 13, null, 0, RequirementType.PRIMARY_ONLY),
    DRUID("Druid", "WIS", 13, null, 0, RequirementType.PRIMARY_ONLY),
    FIGHTER("Fighter", "STR", 13, "DEX", 13, RequirementType.EITHER),
    MONK("Monk", "DEX", 13, "WIS", 13, RequirementType.BOTH),
    PALADIN("Paladin", "STR", 13, "CHA", 13, RequirementType.BOTH),
    RANGER("Ranger", "DEX", 13, "WIS", 13, RequirementType.BOTH),
    ROGUE("Rogue", "DEX", 13, null, 0, RequirementType.PRIMARY_ONLY),
    SORCERER("Sorcerer", "CHA", 13, null, 0, RequirementType.PRIMARY_ONLY),
    WARLOCK("Warlock", "CHA", 13, null, 0, RequirementType.PRIMARY_ONLY),
    WIZARD("Wizard", "INT", 13, null, 0, RequirementType.PRIMARY_ONLY);

    private final String className;
    private final String primaryAttr;
    private final int primaryMin;
    private final String secondaryAttr;
    private final int secondaryMin;
    private final RequirementType type;

    public enum RequirementType {
        PRIMARY_ONLY,
        BOTH,
        EITHER
    }

    MulticlassRequirement(String className, String primaryAttr, int primaryMin,
                          String secondaryAttr, int secondaryMin, RequirementType type) {
        this.className = className;
        this.primaryAttr = primaryAttr;
        this.primaryMin = primaryMin;
        this.secondaryAttr = secondaryAttr;
        this.secondaryMin = secondaryMin;
        this.type = type;
    }

    public boolean isSatisfied(Map<String, Integer> attributes) {
        if (attributes == null) return false;
        int primaryValue = attributes.getOrDefault(primaryAttr, 0);
        if (type == RequirementType.PRIMARY_ONLY) {
            return primaryValue >= primaryMin;
        } else if (type == RequirementType.BOTH) {
            int secondaryValue = attributes.getOrDefault(secondaryAttr, 0);
            return primaryValue >= primaryMin && secondaryValue >= secondaryMin;
        } else { // EITHER
            int secondaryValue = attributes.getOrDefault(secondaryAttr, 0);
            return primaryValue >= primaryMin || secondaryValue >= secondaryMin;
        }
    }

    public static MulticlassRequirement fromClassName(String className) {
        if (className == null) return null;
        for (MulticlassRequirement req : values()) {
            if (req.className.equalsIgnoreCase(className)) return req;
        }
        return null;
    }

    public String getClassName() { return className; }
}
