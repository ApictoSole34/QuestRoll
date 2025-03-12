package com.fizzycoyote.qusetroll.feature_dice.model;

import android.content.Context;

public class Dice {
    private int type;
    private int result;
    private boolean isRolled;
    private boolean isAnimationPlayed;

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

    public int getGifResource(Context context, int rollResult) {
        String gifName = "d" + getType() + "s" + rollResult;
        return context.getResources().getIdentifier(gifName, "drawable", context.getPackageName());
    }

    public int roll() {
        this.result = (int) (Math.random() * type) + 1;
        this.isRolled = true;
        this.isAnimationPlayed = false;
        return result;
    }
}