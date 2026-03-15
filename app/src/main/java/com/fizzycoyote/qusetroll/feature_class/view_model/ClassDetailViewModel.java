package com.fizzycoyote.qusetroll.feature_class.view_model;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.fizzycoyote.qusetroll.core.models.custom.custom_character_class.CustomCharacterClassDao;
import com.fizzycoyote.qusetroll.core.models.custom.custom_character_class.CustomCharacterClassWithFeatures;
import com.fizzycoyote.qusetroll.core.models.open5e.character_class.CharacterClassDao;
import com.fizzycoyote.qusetroll.core.models.open5e.character_class.CharacterClassEntity;
import com.fizzycoyote.qusetroll.core.models.open5e.character_class.CharacterClassWithDetails;
import com.fizzycoyote.qusetroll.core.models.open5e.character_class.feature.FeatureDao;
import com.fizzycoyote.qusetroll.core.models.open5e.character_class.feature.FeatureEntity;
import com.fizzycoyote.qusetroll.core.models.open5e.character_class.hit_points.HitPointsDao;
import com.fizzycoyote.qusetroll.core.models.open5e.character_class.hit_points.HitPointsEntity;
import com.fizzycoyote.qusetroll.core.models.open5e.character_class.saving_throw.SavingThrowDao;
import com.fizzycoyote.qusetroll.core.models.open5e.character_class.saving_throw.SavingThrowEntity;

import java.util.ArrayList;
import java.util.List;

public class ClassDetailViewModel extends ViewModel {
    private final LiveData<CharacterClassEntity> open5eClass;
    private final LiveData<List<FeatureEntity>> open5eFeatures;
    private final LiveData<HitPointsEntity> open5eHitPoints;
    private final LiveData<List<SavingThrowEntity>> open5eSavingThrows;
    private final LiveData<CharacterClassWithDetails> classWithDetails;
    private final LiveData<CustomCharacterClassWithFeatures> customClass;
    private final boolean isCustom;

    public ClassDetailViewModel(CharacterClassDao open5eDao,
                                FeatureDao featureDao,
                                HitPointsDao hitPointsDao,
                                SavingThrowDao savingThrowDao,
                                CustomCharacterClassDao customDao,
                                String classKey) {

        if (classKey.startsWith("custom_")) {
            // Custom class handling
            this.isCustom = true;
            long id = Long.parseLong(classKey.replace("custom_", ""));
            this.customClass = customDao.getClassWithFeatures(id);
            this.open5eClass = null;
            this.open5eFeatures = null;
            this.open5eHitPoints = null;
            this.open5eSavingThrows = null;
            this.classWithDetails = null;
        } else {
            this.isCustom = false;
            this.customClass = null;

            this.open5eClass = open5eDao.getClassByKey(classKey);

            this.open5eFeatures = featureDao.getFeaturesForClass(classKey);

            this.open5eHitPoints = hitPointsDao.getHitPointsForClass(classKey);
            this.open5eSavingThrows = savingThrowDao.getSavingThrowsForClass(classKey);

            MediatorLiveData<CharacterClassWithDetails> mediator = new MediatorLiveData<>();

            mediator.addSource(open5eClass, characterClass ->
                    combineData(mediator, characterClass, open5eFeatures.getValue(),
                            open5eHitPoints.getValue(), open5eSavingThrows.getValue()));

            mediator.addSource(open5eFeatures, features ->
                    combineData(mediator, open5eClass.getValue(), features,
                            open5eHitPoints.getValue(), open5eSavingThrows.getValue()));

            mediator.addSource(open5eHitPoints, hitPoints ->
                    combineData(mediator, open5eClass.getValue(), open5eFeatures.getValue(),
                            hitPoints, open5eSavingThrows.getValue()));

            mediator.addSource(open5eSavingThrows, savingThrows ->
                    combineData(mediator, open5eClass.getValue(), open5eFeatures.getValue(),
                            open5eHitPoints.getValue(), savingThrows));

            this.classWithDetails = mediator;
        }
    }

    private void combineData(MediatorLiveData<CharacterClassWithDetails> mediator,
                             CharacterClassEntity characterClass,
                             List<FeatureEntity> features,
                             HitPointsEntity hitPoints,
                             List<SavingThrowEntity> savingThrows) {

        if (characterClass != null && features != null) {
            CharacterClassWithDetails combined = new CharacterClassWithDetails();
            combined.characterClass = characterClass;
            combined.features = features;

            if (characterClass.subclassOfKey != null) {
                combined.hitPoints = null;
                combined.savingThrows = new ArrayList<>();
            } else {
                combined.hitPoints = hitPoints;
                combined.savingThrows = savingThrows != null ? savingThrows : new ArrayList<>();
            }

            mediator.setValue(combined);
            Log.d("ClassDetailVM", "Successfully combined data for: " + characterClass.name);
        } else {
            Log.d("ClassDetailVM", "Cannot combine data - missing class or features");
        }
    }

    public LiveData<CharacterClassEntity> getOpen5eClass() { return open5eClass; }
    public LiveData<List<FeatureEntity>> getOpen5eFeatures() { return open5eFeatures; }
    public LiveData<HitPointsEntity> getOpen5eHitPoints() { return open5eHitPoints; }
    public LiveData<CharacterClassWithDetails> getClassWithDetails() { return classWithDetails; }
    public LiveData<CustomCharacterClassWithFeatures> getCustomClass() { return customClass; }
    public boolean isCustom() { return isCustom; }

    public static class Factory extends ViewModelProvider.NewInstanceFactory {
        private final CharacterClassDao open5eDao;
        private final FeatureDao featureDao;
        private final HitPointsDao hitPointsDao;
        private final SavingThrowDao savingThrowDao;
        private final CustomCharacterClassDao customDao;
        private final String classKey;

        public Factory(CharacterClassDao open5eDao,
                       FeatureDao featureDao,
                       HitPointsDao hitPointsDao,
                       SavingThrowDao savingThrowDao, CustomCharacterClassDao customDao,
                       String classKey) {
            this.open5eDao = open5eDao;
            this.featureDao = featureDao;
            this.hitPointsDao = hitPointsDao;
            this.savingThrowDao = savingThrowDao;
            this.customDao = customDao;
            this.classKey = classKey;
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            return (T) new ClassDetailViewModel(
                    open5eDao, featureDao, hitPointsDao,savingThrowDao , customDao, classKey
            );
        }
    }
}