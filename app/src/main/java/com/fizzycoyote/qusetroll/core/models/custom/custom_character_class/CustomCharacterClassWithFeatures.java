package com.fizzycoyote.qusetroll.core.models.custom.custom_character_class;

import androidx.room.Embedded;
import androidx.room.Relation;

import com.fizzycoyote.qusetroll.core.models.custom.custom_character_class.custom_feature.CustomFeatureEntity;

import java.util.List;

public class CustomCharacterClassWithFeatures {
    @Embedded
    public CustomCharacterClassEntity characterClassEntity;

    @Relation(
            parentColumn = "id",
            entityColumn = "class_id"
    )
    public List<CustomFeatureEntity> features;
}
