package com.fizzycoyote.qusetroll.feature_background.model;

import com.fizzycoyote.qusetroll.core.models.custom.custom_background.CustomBackgroundEntity;
import com.fizzycoyote.qusetroll.core.models.open5e.background.BackgroundEntity;

import java.util.Objects;

public class CombinedBackground {
    public final String id;
    public final String name;
    public final String documentName;
    public final boolean isCustom;
    public final String key;
    public final long customId;

    public CombinedBackground(BackgroundEntity b) {
        this.id = b.key;
        this.key = b.key;
        this.customId = -1;
        this.name = b.name;
        this.documentName = b.documentName;
        this.isCustom = false;
    }

    public CombinedBackground(CustomBackgroundEntity b) {
        this.id = "custom_" + b.id;
        this.key = null;
        this.customId = b.id;
        this.name = b.name;
        this.documentName = "Custom";
        this.isCustom = true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return Objects.equals(id, ((CombinedBackground) o).id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }
}