package com.fizzycoyote.qusetroll.feature_species.model;

import com.fizzycoyote.qusetroll.core.models.custom.custom_species.CustomSpeciesEntity;
import com.fizzycoyote.qusetroll.core.models.open5e.species.SpeciesEntity;

import java.util.Objects;

public class CombinedSpecies {
    public final String id;
    public final String name;
    public final boolean isSubspecies;
    public final String subspeciesOfName;
    public final String documentName;
    public final boolean isCustom;
    public final String key;
    public final long customId;

    public CombinedSpecies(SpeciesEntity s) {
        this.id = s.key;
        this.key = s.key;
        this.customId = -1;
        this.name = s.name;
        this.isSubspecies = s.isSubspecies;
        this.subspeciesOfName = s.subspeciesOf;
        this.documentName = s.documentName;
        this.isCustom = false;
    }

    public CombinedSpecies(CustomSpeciesEntity s) {
        this.id = "custom_" + s.id;
        this.key = null;
        this.customId = s.id;
        this.name = s.name;
        this.isSubspecies = s.isSubspecies;
        this.subspeciesOfName = s.subspeciesOfName;
        this.documentName = "Custom";
        this.isCustom = true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return Objects.equals(id, ((CombinedSpecies) o).id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }
}