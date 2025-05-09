package com.fizzycoyote.qusetroll.feature_language.model.item;

public class ScriptItem {
    final String name;
    public final String scriptIdentifier;

    public ScriptItem(String name, String scriptIdentifier) {
        this.name = name;
        this.scriptIdentifier = scriptIdentifier;
    }

    @Override
    public String toString() {
        return name;
    }
}