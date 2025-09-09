package com.fizzycoyote.qusetroll.feature_class.view_model;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.fizzycoyote.qusetroll.core.models.custom.custom_character_class.CustomCharacterClassDao;
import com.fizzycoyote.qusetroll.core.models.custom.custom_character_class.CustomCharacterClassEntity;
import com.fizzycoyote.qusetroll.core.models.custom.custom_character_class.CustomCharacterClassWithFeatures;
import com.fizzycoyote.qusetroll.core.models.custom.custom_character_class.custom_feature.CustomFeatureDao;
import com.fizzycoyote.qusetroll.core.models.custom.custom_character_class.custom_feature.CustomFeatureEntity;
import com.fizzycoyote.qusetroll.core.models.open5e.ability.AbilityDao;
import com.fizzycoyote.qusetroll.core.models.open5e.ability.AbilityEntity;
import com.fizzycoyote.qusetroll.core.models.open5e.character_class.CharacterClassDao;
import com.fizzycoyote.qusetroll.core.models.open5e.character_class.CharacterClassEntity;
import com.fizzycoyote.qusetroll.feature_class.model.CombinedClass;
import com.fizzycoyote.qusetroll.feature_class.repository.ClassRepository;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class ClassCreateViewModel extends ViewModel {
    private final ClassRepository repository;
    private final MutableLiveData<List<CombinedClass>> baseClasses = new MutableLiveData<>();
    private final MutableLiveData<List<CustomFeatureEntity>> features = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Set<String>> selectedSavingThrows = new MutableLiveData<>(new HashSet<>());
    private final MutableLiveData<Boolean> saveResult = new MutableLiveData<>();

    public ClassCreateViewModel(@NonNull ClassRepository repository) {
        this.repository = repository;
        loadBaseClasses();
    }

    private void loadBaseClasses() {
        repository.getCombinedClasses().observeForever(combined -> {
            List<CombinedClass> filtered = combined.stream()
                    .filter(cc -> cc.getSubclassOf() == null)
                    .collect(Collectors.toList());
            baseClasses.postValue(filtered);
        });
    }

    public void addFeature(CustomFeatureEntity feature) {
        List<CustomFeatureEntity> current = features.getValue();
        current.add(feature);
        features.postValue(current);
    }

    public void saveClass(CustomCharacterClassEntity entity) {
        repository.getExecutor().execute(() -> {
            try {
                if (repository.getCustomDao().countByName(entity.name) > 0) {
                    saveResult.postValue(false);
                    return;
                }

                long classId = repository.getCustomDao().insertClass(entity);

                List<CustomFeatureEntity> updatedFeatures = features.getValue().stream()
                        .peek(f -> f.classId = classId)
                        .collect(Collectors.toList());

                repository.getCustomDao().insertFeatures(updatedFeatures);
                saveResult.postValue(true);
            } catch (Exception e) {
                saveResult.postValue(false);
            }
        });
    }

    // Gettery
    public LiveData<List<CombinedClass>> getBaseClasses() { return baseClasses; }
    public LiveData<List<CustomFeatureEntity>> getFeatures() { return features; }
    public LiveData<Boolean> getSaveResult() { return saveResult; }
    public LiveData<Set<String>> getSelectedSavingThrows() {
        return selectedSavingThrows;
    }
    // Dodaj metodę do aktualizacji wartości
    public void setSelectedSavingThrows(Set<String> selectedThrows) {
        selectedSavingThrows.setValue(selectedThrows);
    }


    public void updateSavingThrows(Set<String> selectedAbilities) {
        selectedSavingThrows.postValue(selectedAbilities);
    }
}