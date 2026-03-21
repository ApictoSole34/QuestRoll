package com.fizzycoyote.qusetroll.feature_species.model;

public class SpeciesFilter {
    public String query = "";
    public boolean subspeciesOnly = false;
    public boolean mainOnly = false;
    public String source = "";

    public boolean isEmpty() {
        return query.isEmpty()
                && !subspeciesOnly
                && !mainOnly
                && source.isEmpty();
    }
}
