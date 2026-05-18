package com.fizzycoyote.qusetroll.feature_character.utils;

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

    public static String getCastingAbilityForClass(String className) {
        for (ClassCastingAbility cca : values()) {
            if (cca.className.equalsIgnoreCase(className)) {
                return cca.castingAbility;
            }
        }
        return "INT"; // fallback for unknown classes
    }
}