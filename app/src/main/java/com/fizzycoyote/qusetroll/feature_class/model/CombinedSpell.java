package com.fizzycoyote.qusetroll.feature_class.model;

public class CombinedSpell {
    private final String key;
    private final String name;
    private final int level;
    private final boolean custom;

    public CombinedSpell(String key, String name, int level, boolean custom) {
        this.key = key;
        this.name = name;
        this.level = level;
        this.custom = custom;
    }

    public String getKey() { return key; }
    public String getName() { return name; }
    public int getLevel() { return level; }
    public boolean isCustom() { return custom; }
}
