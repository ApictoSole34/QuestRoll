package com.fizzycoyote.qusetroll.feature_character.utils;

/**
 * Defines starting gold generation rules for each official D&D 5e class.
 * <p>
 * According to standard rules, starting gold is usually determined by a dice roll
 * (typically Xd4) multiplied by 10 gp.
 * </p>
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
     * Finds the enum entry corresponding to a class name.
     *
     * @param className The name of the class (e.g., "Wizard").
     * @return The matching {@link ClassStartingGold} entry, or {@code FIGHTER} as a fallback.
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
     * Simulates the dice roll to determine starting gold.
     *
     * @return The calculated gold amount in gp.
     */
    public int rollGold() {
        int total = 0;
        for (int i = 0; i < diceCount; i++) {
            total += (int) (Math.random() * diceSides) + 1;
        }
        return total * multiplier;
    }

    /**
     * Returns a human-readable description of the gold roll formula.
     *
     * @return A string like "5d4 × 10".
     */
    public String getDescription() {
        return diceCount + "d" + diceSides + " × " + multiplier;
    }

    public String getClassName() { return className; }
    public int getDiceCount() { return diceCount; }
    public int getDiceSides() { return diceSides; }
    public int getMultiplier() { return multiplier; }
}
