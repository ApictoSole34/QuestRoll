package com.fizzycoyote.qusetroll.feature_item.model;

public class ItemFilter {
    public String query = "";
    public String categoryName = "";
    public String source = "";
    public boolean magicOnly = false;
    public String rarity = "";
    public boolean requiresAttunement = false;
    public String weaponProperty = "";

    public boolean isEmpty() {
        return query.isEmpty() && categoryName.isEmpty() && source.isEmpty() && !magicOnly
                && rarity.isEmpty() && !requiresAttunement;
    }
}