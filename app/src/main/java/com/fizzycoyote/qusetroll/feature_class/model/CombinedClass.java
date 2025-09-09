package com.fizzycoyote.qusetroll.feature_class.model;

import java.util.Objects;

public class CombinedClass {
    public final String id;
    public final String name;
    public final boolean isCustom;
    public final String parentKey;
    public final String parentName;

    public String getSubclassOf() { return parentKey; }
    public String getName() { return name; }
    public String getKey() { return id; }

    @Override
    public String toString() {
        return name;
    }

    public CombinedClass(String id,
                         String name,
                         boolean isCustom,
                         String parentKey,
                         String parentName) {
        this.id = id;
        this.name = name;
        this.isCustom = isCustom;
        this.parentKey = parentKey;
        this.parentName = parentName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CombinedClass that = (CombinedClass) o;
        return isCustom == that.isCustom &&
                Objects.equals(id, that.id) &&
                Objects.equals(name, that.name) &&
                Objects.equals(parentKey, that.parentKey) &&
                Objects.equals(parentName, that.parentName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, isCustom, parentKey, parentName);
    }
}