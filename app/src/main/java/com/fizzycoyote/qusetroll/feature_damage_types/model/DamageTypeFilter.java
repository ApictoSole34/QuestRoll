package com.fizzycoyote.qusetroll.feature_damage_types.model;

public class DamageTypeFilter {
    public String query = "";
    public boolean customOnly = false;

    public boolean isEmpty() {
        return query.isEmpty() && !customOnly;
    }
}