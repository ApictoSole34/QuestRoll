package com.fizzycoyote.qusetroll.feature_character.utils;

import java.util.Map;

/**
 * Attribute requirements for multiclassing (D&D 5e 2014/2024).
 * The starting class (first) does not need to satisfy these requirements.
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
    WARLOCK("Warlock", "CHA", 13, null, 0, RequirementType.PRIMARY_ONLY),
    WIZARD("Wizard", "INT", 13, null, 0, RequirementType.PRIMARY_ONLY);

    private final String className;
    private final String primaryAttr;
    private final int primaryMin;
    private final String secondaryAttr;
    private final int secondaryMin;
    private final RequirementType type;

    public enum RequirementType {
        PRIMARY_ONLY,   // only primaryAttr >= primaryMin
        BOTH,           // primaryAttr >= primaryMin AND secondaryAttr >= secondaryMin
        EITHER          // primaryAttr >= primaryMin OR secondaryAttr >= secondaryMin
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

    /**
     * Checks whether the character meets the multiclass requirements for the class.
     * @param attributes map of ability scores (keys: STR, DEX, CON, INT, WIS, CHA)
     * @return true if requirements are met, false otherwise
     */
    public boolean isSatisfied(Map<String, Integer> attributes) {
        int primaryValue = attributes.getOrDefault(primaryAttr, 0);
        if (type == RequirementType.PRIMARY_ONLY) {
            return primaryValue >= primaryMin;
        } else if (type == RequirementType.BOTH) {
            int secondaryValue = attributes.getOrDefault(secondaryAttr, 0);
            return primaryValue >= primaryMin && secondaryValue >= secondaryMin;
        } else { // EITHER (used only for Fighter)
            int secondaryValue = attributes.getOrDefault(secondaryAttr, 0);
            return primaryValue >= primaryMin || secondaryValue >= secondaryMin;
        }
    }

    /**
     * Finds the enum entry for the given class name (case-insensitive).
     */
    public static MulticlassRequirement fromClassName(String className) {
        for (MulticlassRequirement req : values()) {
            if (req.className.equalsIgnoreCase(className)) return req;
        }
        return null;
    }

    public String getClassName() { return className; }
    public String getPrimaryAttr() { return primaryAttr; }
    public int getPrimaryMin() { return primaryMin; }
    public String getSecondaryAttr() { return secondaryAttr; }
    public int getSecondaryMin() { return secondaryMin; }
    public RequirementType getType() { return type; }
}