package com.fizzycoyote.qusetroll.feature_item.model.weapon;

public class WeaponFilter {
    public String query = "";
    public boolean simpleOnly = false;
    public boolean martialOnly = false;
    public String source = "";

    public boolean isEmpty() {
        return query.isEmpty() && !simpleOnly && !martialOnly && source.isEmpty();
    }
}