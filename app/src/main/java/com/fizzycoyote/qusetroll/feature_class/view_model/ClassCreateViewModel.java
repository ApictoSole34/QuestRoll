package com.fizzycoyote.qusetroll.feature_class.view_model;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.fizzycoyote.qusetroll.core.models.custom.custom_character_class.CustomCharacterClassEntity;
import com.fizzycoyote.qusetroll.core.models.custom.custom_character_class.CustomCharacterClassWithFeatures;
import com.fizzycoyote.qusetroll.core.models.custom.custom_character_class.custom_feature.CustomFeatureEntity;
import com.fizzycoyote.qusetroll.feature_class.model.CombinedClass;
import com.fizzycoyote.qusetroll.feature_class.repository.ClassRepository;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ClassCreateViewModel extends ViewModel {

    public static final long NO_ID = -1L;

    private final ClassRepository repository;
    private final long editClassId;

    private final MutableLiveData<List<CustomFeatureEntity>> features =
            new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Set<String>> selectedSavingThrows =
            new MutableLiveData<>(new HashSet<>());
    private final MutableLiveData<Boolean> saveResult = new MutableLiveData<>();

    private final MutableLiveData<CustomCharacterClassWithFeatures> editData =
            new MutableLiveData<>();

    public ClassCreateViewModel(@NonNull ClassRepository repository, long editClassId) {
        this.repository = repository;
        this.editClassId = editClassId;

        if (isEditMode()) {
            loadExistingClass();
        }
    }

    public boolean isEditMode() {
        return editClassId != NO_ID;
    }

    private void loadExistingClass() {
        repository.getCustomDao().getClassWithFeatures(editClassId)
                .observeForever(data -> {
                    if (data == null) return;

                    if (editData.getValue() == null) {
                        editData.setValue(data);

                        features.setValue(data.features != null
                                ? new ArrayList<>(data.features)
                                : new ArrayList<>());

                        if (data.characterClassEntity.savingThrows != null) {
                            selectedSavingThrows.setValue(
                                    new HashSet<>(data.characterClassEntity.savingThrows));
                        }
                    }
                });
    }

    public LiveData<List<CombinedClass>> getBaseClasses() {
        return Transformations.map(repository.getCombinedClasses(), combined ->
                combined.stream()
                        .filter(cc -> cc.getSubclassOf() == null)
                        .collect(Collectors.toList())
        );
    }

    public LiveData<List<CustomFeatureEntity>> getFeatures() { return features; }
    public LiveData<Set<String>> getSelectedSavingThrows() { return selectedSavingThrows; }
    public LiveData<Boolean> getSaveResult() { return saveResult; }
    public LiveData<CustomCharacterClassWithFeatures> getEditData() { return editData; }

    public void setSelectedSavingThrows(Set<String> throws_) {
        selectedSavingThrows.setValue(throws_);
    }

    public void addFeature(CustomFeatureEntity feature) {
        List<CustomFeatureEntity> current = new ArrayList<>(
                features.getValue() != null ? features.getValue() : new ArrayList<>());
        current.add(feature);
        features.setValue(current);
    }

    public void updateFeature(int index, CustomFeatureEntity feature) {
        List<CustomFeatureEntity> current = new ArrayList<>(
                features.getValue() != null ? features.getValue() : new ArrayList<>());
        if (index >= 0 && index < current.size()) {
            current.set(index, feature);
            features.setValue(current);
        }
    }

    public void removeFeature(int index) {
        List<CustomFeatureEntity> current = new ArrayList<>(
                features.getValue() != null ? features.getValue() : new ArrayList<>());
        if (index >= 0 && index < current.size()) {
            current.remove(index);
            features.setValue(current);
        }
    }

    public void saveClass(CustomCharacterClassEntity entity) {
        repository.getExecutor().execute(() -> {
            try {
                if (isEditMode()) {
                    updateExistingClass(entity);
                } else {
                    createNewClass(entity);
                }
            } catch (Exception e) {
                saveResult.postValue(false);
            }
        });
    }

    private void createNewClass(CustomCharacterClassEntity entity) {
        if (repository.getCustomDao().countByName(entity.name) > 0) {
            saveResult.postValue(false);
            return;
        }
        long classId = repository.getCustomDao().insertClass(entity);
        insertFeatures(classId);
        saveResult.postValue(true);
    }

    private void updateExistingClass(CustomCharacterClassEntity entity) {
        entity.id = editClassId;
        repository.getCustomDao().updateClass(entity);

        repository.getCustomDao().deleteFeaturesForClass(editClassId);
        insertFeatures(editClassId);
        saveResult.postValue(true);
    }

    private void insertFeatures(long classId) {
        List<CustomFeatureEntity> toInsert =
                features.getValue() != null ? features.getValue() : new ArrayList<>();

        List<CustomFeatureEntity> withId = toInsert.stream()
                .map(f -> { f.classId = classId; return f; })
                .collect(Collectors.toList());

        repository.getCustomDao().insertFeatures(withId);
    }
}