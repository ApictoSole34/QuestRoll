package com.fizzycoyote.qusetroll.feature_class.view_model;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.fizzycoyote.qusetroll.core.models.custom.custom_character_class.CustomCharacterClassDao;
import com.fizzycoyote.qusetroll.core.models.open5e.character_class.CharacterClassDao;
import com.fizzycoyote.qusetroll.core.models.open5e.character_class.feature.FeatureDao;
import com.fizzycoyote.qusetroll.core.models.open5e.character_class.hit_points.HitPointsDao;

public class ClassDetailViewModelFactory implements ViewModelProvider.Factory {
    private final CharacterClassDao open5eDao;
    private final FeatureDao featureDao;
    private final HitPointsDao hitPointsDao;
    private final CustomCharacterClassDao customDao;
    private final String classKey;

    public ClassDetailViewModelFactory(CharacterClassDao open5eDao,
                                       FeatureDao featureDao,
                                       HitPointsDao hitPointsDao,
                                       CustomCharacterClassDao customDao,
                                       String classKey) {
        this.open5eDao = open5eDao;
        this.featureDao = featureDao;
        this.hitPointsDao = hitPointsDao;
        this.customDao = customDao;
        this.classKey = classKey;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new ClassDetailViewModel(
                open5eDao, featureDao, hitPointsDao, customDao, classKey
        );
    }
}