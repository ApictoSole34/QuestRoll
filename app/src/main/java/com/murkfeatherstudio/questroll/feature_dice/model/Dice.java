package com.murkfeatherstudio.questroll.feature_dice.model;

import android.content.Context;

/**
 * Model representing a polyhedral die (e.g., d6, d20).
 * <p>
 * This class handles the logic for rolling the die, tracking its state, and
 * determining the appropriate visual asset (GIF) to display based on the result.
 * </p>
 */
public class Dice {
    private int type;
    private int result;
    private boolean isRolled;
    private boolean isAnimationPlayed;

    /**
     * Creates a new die of the specified type.
     *
     * @param type The number of sides (e.g., 6 for a d6).
     */
    public Dice(int type) {
        this.type = type;
        this.result = 1;
        this.isRolled = false;
        this.isAnimationPlayed = false;
    }

    public int getType() {
        return type;
    }

    public int getResult() {
        return result;
    }

    /**
     * Gets the name of the drawable resource (GIF) for the current roll result.
     *
     * @return Resource name string (e.g., "d20s15").
     */
    public String getGifName() {
        return "d" + type + "s" + result;
    }

    public boolean isRolled() {
        return isRolled;
    }

    public boolean isAnimationPlayed() {
        return isAnimationPlayed;
    }

    public void setAnimationPlayed(boolean animationPlayed) {
        isAnimationPlayed = animationPlayed;

    }

    /**
     * Resolves the resource ID for the roll animation GIF.
     *
     * @param context    Application context to access resources.
     * @param rollResult The numeric result of the roll.
     * @return The integer resource ID of the matching drawable.
     */
    public int getGifResource(Context context, int rollResult) {
        String gifName = "d" + getType() + "s" + rollResult;
        return context.getResources().getIdentifier(gifName, "drawable", context.getPackageName());
    }

    /**
     * Simulates a die roll by generating a random integer between 1 and the die type.
     *
     * @return The result of the roll.
     */
    public int roll() {
        this.result = (int) (Math.random() * type) + 1;
        this.isRolled = true;
        this.isAnimationPlayed = false;
        return result;
    }
}
