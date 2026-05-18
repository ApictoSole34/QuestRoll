package com.fizzycoyote.qusetroll.feature_character.utils;

/**
 * Defines starting gold generation rules for each class.
 * For D&D 5e: Xd4 × 10 gp.
 */
public enum ClassStartingGold {
    FIGHTER("Fighter", 5, 4, 10),
    WIZARD("Wizard", 4, 4, 10),
    ROGUE("Rogue", 5, 4, 10),
    CLERIC("Cleric", 5, 4, 10),
    BARBARIAN("Barbarian", 5, 4, 10),
    BARD("Bard", 5, 4, 10),
    DRUID("Druid", 2, 4, 10),
    MONK("Monk", 5, 4, 10),
    PALADIN("Paladin", 5, 4, 10),
    RANGER("Ranger", 5, 4, 10),
    SORCERER("Sorcerer", 3, 4, 10),
    WARLOCK("Warlock", 4, 4, 10);

    private final String className;
    private final int diceCount;
    private final int diceSides;
    private final int multiplier;

    ClassStartingGold(String className, int diceCount, int diceSides, int multiplier) {
        this.className = className;
        this.diceCount = diceCount;
        this.diceSides = diceSides;
        this.multiplier = multiplier;
    }

    /**
     * Finds the enum entry by class name (case-insensitive).
     */
    public static ClassStartingGold fromClassName(String className) {
        for (ClassStartingGold value : values()) {
            if (value.className.equalsIgnoreCase(className)) {
                return value;
            }
        }
        return FIGHTER; // default fallback
    }

    /**
     * Rolls the dice and returns the starting gold amount.
     */
    public int rollGold() {
        int total = 0;
        for (int i = 0; i < diceCount; i++) {
            total += (int) (Math.random() * diceSides) + 1;
        }
        return total * multiplier;
    }

    /**
     * Returns a description of the roll, e.g. "5d4 × 10".
     */
    public String getDescription() {
        return diceCount + "d" + diceSides + " × " + multiplier;
    }

    public String getClassName() { return className; }
    public int getDiceCount() { return diceCount; }
    public int getDiceSides() { return diceSides; }
    public int getMultiplier() { return multiplier; }
}