package com.fizzycoyote.qusetroll.feature_spell.model;

public class SpellFilter {
    public String query = "";
    public int level = -1;
    public String schoolKey = "";
    public boolean ritualOnly = false;
    public boolean concentrationOnly = false;
    public String source = "";

    public boolean isEmpty() {
        return query.isEmpty()
                && level == -1
                && schoolKey.isEmpty()
                && !ritualOnly
                && !concentrationOnly
                && source.isEmpty();
    }
}