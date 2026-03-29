package com.fizzycoyote.qusetroll.feature_damage_types.model;

import com.fizzycoyote.qusetroll.core.models.custom.custom_damage_types.CustomDamageTypeEntity;
import com.fizzycoyote.qusetroll.core.models.open5e.damage_type.DamageTypeEntity;

public class CombinedDamageType {
    public final String id;
    public final String name;
    public final String description;
    public final String source;
    public final boolean isCustom;
    public final String key;
    public final long customId;

    public CombinedDamageType(DamageTypeEntity t) {
        this.id = t.key;
        this.key = t.key;
        this.customId = -1;
        this.name = t.name;
        this.description = t.description;

        String sourceName = "SRD";
        if (t.document != null && !t.document.isEmpty()) {
            String url = t.document;
            if (url.endsWith("/")) url = url.substring(0, url.length() - 1);
            String[] parts = url.split("/");
            if (parts.length > 0) {
                String last = parts[parts.length - 1];
                if (!last.isEmpty()) sourceName = last;
            }
        }
        this.source = sourceName;
        this.isCustom = false;
    }

    public CombinedDamageType(CustomDamageTypeEntity t) {
        this.id = "custom_" + t.id;
        this.key = null;
        this.customId = t.id;
        this.name = t.name;
        this.description = t.description;
        this.source = "Custom";
        this.isCustom = true;
    }
}