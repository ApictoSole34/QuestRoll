package com.murkfeatherstudio.questroll.feature_creature.model;

public class CreatureFilter {
    public String query = "";
    public String typeKey = "";
    public String alignment = "";
    public float crMin = -1f;
    public float crMax = -1f;
    public String source = "";

    public boolean isEmpty() {
        return query.isEmpty()
                && typeKey.isEmpty()
                && alignment.isEmpty()
                && crMin < 0
                && crMax < 0
                && source.isEmpty();
    }
}