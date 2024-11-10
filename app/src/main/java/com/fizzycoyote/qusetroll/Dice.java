package com.fizzycoyote.qusetroll;

import android.content.Context;

public class Dice {
    private final int diceType;
    private final Context context;

    public Dice(int diceType, Context context) {
        this.diceType = diceType;
        this.context = context;
    }

    public int roll() {
        return (int) (Math.random() * diceType) + 1;
    }

    public int getGifResourceId(int rollResult) {
        String gifName = "d" + diceType + "s" + rollResult;
        return context.getResources().getIdentifier(gifName, "drawable", context.getPackageName());
    }

    public int getDiceType() {
        return diceType;
    }

}
