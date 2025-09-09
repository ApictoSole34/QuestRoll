package com.fizzycoyote.qusetroll.feature_class.view_model;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.fizzycoyote.qusetroll.core.models.custom.custom_character_class.CustomCharacterClassDao;
import com.fizzycoyote.qusetroll.core.models.custom.custom_character_class.CustomCharacterClassWithFeatures;
import com.fizzycoyote.qusetroll.core.models.custom.custom_character_class.custom_feature.CustomFeatureEntity;
import com.fizzycoyote.qusetroll.core.models.open5e.character_class.CharacterClassDao;
import com.fizzycoyote.qusetroll.core.models.open5e.character_class.CharacterClassEntity;
import com.fizzycoyote.qusetroll.core.models.open5e.character_class.feature.FeatureDao;
import com.fizzycoyote.qusetroll.core.models.open5e.character_class.feature.FeatureEntity;
import com.fizzycoyote.qusetroll.core.models.open5e.character_class.hit_points.HitPointsDao;
import com.fizzycoyote.qusetroll.core.models.open5e.character_class.hit_points.HitPointsEntity;

import java.util.List;

public class ClassDetailViewModel extends ViewModel {
    private final LiveData<CharacterClassEntity> open5eClass;
    private final LiveData<List<FeatureEntity>> open5eFeatures;
    private final LiveData<HitPointsEntity> open5eHitPoints;

    private final LiveData<CustomCharacterClassWithFeatures> customClass;
    private final boolean isCustom;

    public ClassDetailViewModel(CharacterClassDao open5eDao,
                                FeatureDao featureDao,
                                HitPointsDao hitPointsDao,
                                CustomCharacterClassDao customDao,
                                String classKey) {

        if (classKey.startsWith("custom_")) {
            this.isCustom = true;
            long id = Long.parseLong(classKey.replace("custom_", ""));
            this.customClass = customDao.getClassWithFeatures(id);
            this.open5eClass = null;
            this.open5eFeatures = null;
            this.open5eHitPoints = null;
        } else {
            this.isCustom = false;
            this.customClass = null;
            this.open5eClass = open5eDao.getClassByKey(classKey);
            this.open5eFeatures = featureDao.getFeaturesForClass(classKey);
            this.open5eHitPoints = hitPointsDao.getHitPointsForClass(classKey);
        }
    }

    public LiveData<CharacterClassEntity> getOpen5eClass() { return open5eClass; }
    public LiveData<List<FeatureEntity>> getOpen5eFeatures() { return open5eFeatures; }
    public LiveData<HitPointsEntity> getOpen5eHitPoints() { return open5eHitPoints; }
    public LiveData<CustomCharacterClassWithFeatures> getCustomClass() { return customClass; }
    public boolean isCustom() { return isCustom; }

    public static class Factory extends ViewModelProvider.NewInstanceFactory {
        private final CharacterClassDao open5eDao;
        private final FeatureDao featureDao;
        private final HitPointsDao hitPointsDao;
        private final CustomCharacterClassDao customDao;
        private final String classKey;

        public Factory(CharacterClassDao open5eDao,
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
}