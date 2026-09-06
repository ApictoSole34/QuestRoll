package com.fizzycoyote.qusetroll.feature_character.utils;

/**
 * Enumeration defining the primary spellcasting ability for each official D&D 5e class.
 * <p>
 * This is used to determine which attribute modifier (INT, WIS, or CHA) should be
 * used for spell save DCs and spell attack rolls.
 * </p>
 */
public enum ClassCastingAbility {
    BARBARIAN("Barbarian", null),
    BARD("Bard", "CHA"),
    CLERIC("Cleric", "WIS"),
    DRUID("Druid", "WIS"),
    FIGHTER("Fighter", null),
    MONK("Monk", null),
    PALADIN("Paladin", "CHA"),
    RANGER("Ranger", "WIS"),
    ROGUE("Rogue", null),
    SORCERER("Sorcerer", "CHA"),
    WARLOCK("Warlock", "CHA"),
    WIZARD("Wizard", "INT");

    private final String className;
    private final String castingAbility; // "STR", "DEX", "CON", "INT", "WIS", "CHA" or null

    ClassCastingAbility(String className, String castingAbility) {
        this.className = className;
        this.castingAbility = castingAbility;
    }

    /**
     * Retrieves the spellcasting ability key for a given class name.
     *
     * @param className The name of the class.
     * @return The ability key (e.g., "WIS"), or "INT" as a fallback for unknown classes.
     */
    public static String getCastingAbilityForClass(String className) {
        for (ClassCastingAbility cca : values()) {
            if (cca.className.equalsIgnoreCase(className)) {
                return cca.castingAbility;
            }
        }
        return "INT"; // fallback for unknown classes
    }
}
