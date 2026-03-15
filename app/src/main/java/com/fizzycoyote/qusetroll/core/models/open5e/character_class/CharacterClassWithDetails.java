package com.fizzycoyote.qusetroll.core.models.open5e.character_class;

import androidx.room.Embedded;
import androidx.room.Relation;

import com.fizzycoyote.qusetroll.core.models.open5e.character_class.feature.FeatureEntity;
import com.fizzycoyote.qusetroll.core.models.open5e.character_class.hit_points.HitPointsEntity;
import com.fizzycoyote.qusetroll.core.models.open5e.character_class.saving_throw.SavingThrowEntity;

import java.util.List;

public class CharacterClassWithDetails {
    @Embedded
    public CharacterClassEntity characterClass;

    @Relation(
            parentColumn = "class_key",
            entityColumn = "class_key_ref",
            entity = SavingThrowEntity.class
    )
    public List<SavingThrowEntity> savingThrows;

    @Relation(
            parentColumn = "class_key",
            entityColumn = "class_key_ref",
            entity = FeatureEntity.class
    )
    public List<FeatureEntity> features;

    @Relation(
            parentColumn = "class_key",
            entityColumn = "class_key_ref",
            entity = HitPointsEntity.class
    )
    public HitPointsEntity hitPoints;
}

