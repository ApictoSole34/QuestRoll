package com.fizzycoyote.qusetroll.feature_class.view_model;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.fizzycoyote.qusetroll.core.models.custom.custom_character_class.CustomCharacterClassEntity;
import com.fizzycoyote.qusetroll.core.models.custom.custom_character_class.CustomCharacterClassWithFeatures;
import com.fizzycoyote.qusetroll.core.models.custom.custom_character_class.custom_feature.CustomFeatureEntity;
import com.fizzycoyote.qusetroll.feature_class.model.CombinedClass;
import com.fizzycoyote.qusetroll.feature_class.repository.ClassRepository;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * ViewModel for creating and editing custom character classes.
 * <p>
 * This class manages the state for the custom class creation form, including its features,
 * saving throws, skill options, and starting equipment. It supports loading existing custom
 * classes for modification.
 * </p>
 */
public class ClassCreateViewModel extends ViewModel {

    public static final long NO_ID = -1L;

    private final ClassRepository repository;
    private final long editClassId;

    private final MutableLiveData<List<CustomFeatureEntity>> features =
            new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Set<String>> selectedSavingThrows =
            new MutableLiveData<>(new HashSet<>());
    private final MutableLiveData<Boolean> saveResult = new MutableLiveData<>();

    private final MutableLiveData<List<String>> skillOptions =
            new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<String>> languageKeys =
            new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<CustomCharacterClassWithFeatures> editData =
            new MutableLiveData<>();

    private final MutableLiveData<List<String>> startingItems =
            new MutableLiveData<>(new ArrayList<>());

    private LiveData<CustomCharacterClassWithFeatures> editLiveData;
    private Observer<CustomCharacterClassWithFeatures> editObserver;

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

    public LiveData<List<String>> getStartingItems() { return startingItems; }

    public void addStartingItem(String itemName) {
        List<String> list = new ArrayList<>(startingItems.getValue());
        list.add(itemName);
        startingItems.setValue(list);
    }

    public void removeStartingItem(int position) {
        List<String> list = new ArrayList<>(startingItems.getValue());
        if (position >= 0 && position < list.size()) list.remove(position);
        startingItems.setValue(list);
    }

    /**
     * Loads an existing custom class and populates the ViewModel state for editing.
     */
    private void loadExistingClass() {
        editLiveData = repository.getCustomDao().getClassWithFeatures(editClassId);
        editObserver = data -> {
            if (data == null) return;
            if (editData.getValue() == null) {
                editData.setValue(data);

                features.setValue(data.features != null
                        ? new ArrayList<>(data.features)
                        : new ArrayList<>());

                if (data.characterClassEntity.startingItemsJson != null && !data.characterClassEntity.startingItemsJson.isEmpty()) {
                    try {
                        Type listType = new TypeToken<List<String>>(){}.getType();
                        List<String> loaded = new Gson().fromJson(
                                data.characterClassEntity.startingItemsJson, listType);
                        startingItems.setValue(new ArrayList<>(loaded));
                    } catch (Exception ignored) {}
                }

                if (data.characterClassEntity.savingThrows != null) {
                    selectedSavingThrows.setValue(
                            new HashSet<>(data.characterClassEntity.savingThrows));
                }

                if (data.characterClassEntity.skillOptionsJson != null &&
                        !data.characterClassEntity.skillOptionsJson.isEmpty()) {
                    try {
                        Type listType = new TypeToken<List<String>>(){}.getType();
                        List<String> loaded = new Gson().fromJson(
                                data.characterClassEntity.skillOptionsJson, listType);
                        skillOptions.setValue(new ArrayList<>(loaded));
                    } catch (Exception ignored) {}
                }

                if (data.characterClassEntity.languageKeysJson != null &&
                        !data.characterClassEntity.languageKeysJson.isEmpty()) {
                    try {
                        Type listType = new TypeToken<List<String>>(){}.getType();
                        List<String> loaded = new Gson().fromJson(
                                data.characterClassEntity.languageKeysJson, listType);
                        languageKeys.setValue(new ArrayList<>(loaded));
                    } catch (Exception ignored) {}
                }
            }
        };
        editLiveData.observeForever(editObserver);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (editLiveData != null && editObserver != null) {
            editLiveData.removeObserver(editObserver);
        }
    }

    /**
     * Retrieves all potential parent classes for subclass creation.
     *
     * @return LiveData list of base {@link CombinedClass} objects.
     */
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
    public LiveData<List<String>> getSkillOptions() { return skillOptions; }
    public LiveData<List<String>> getLanguageKeys() { return languageKeys; }

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

    public void addSkillOption(String skillKey) {
        List<String> list = new ArrayList<>(skillOptions.getValue());
        list.add(skillKey);
        skillOptions.setValue(list);
    }

    public void removeSkillOption(int position) {
        List<String> list = new ArrayList<>(skillOptions.getValue());
        if (position >= 0 && position < list.size()) list.remove(position);
        skillOptions.setValue(list);
    }

    public void addLanguageKey(String key) {
        List<String> list = new ArrayList<>(languageKeys.getValue());
        list.add(key);
        languageKeys.setValue(list);
    }

    public void removeLanguageKey(int position) {
        List<String> list = new ArrayList<>(languageKeys.getValue());
        if (position >= 0 && position < list.size()) list.remove(position);
        languageKeys.setValue(list);
    }

    /**
     * Persists the custom class to the database.
     *
     * @param entity          The class entity containing basic info.
     * @param languageChoices Number of additional languages the user can choose.
     */
    public void saveClass(CustomCharacterClassEntity entity, int languageChoices) {
        repository.getExecutor().execute(() -> {
            try {
                Gson gson = new Gson();
                entity.skillOptionsJson = gson.toJson(skillOptions.getValue());
                entity.languageKeysJson = gson.toJson(languageKeys.getValue());
                entity.languageChoices = languageChoices;
                entity.startingItemsJson = gson.toJson(startingItems.getValue());

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

    /**
     * Inserts the list of features associated with the class, ensuring they are correctly
     * linked via {@code classId}.
     */
    private void insertFeatures(long classId) {
        List<CustomFeatureEntity> toInsert =
                features.getValue() != null ? features.getValue() : new ArrayList<>();

        List<CustomFeatureEntity> copies = toInsert.stream()
                .map(original -> {
                    CustomFeatureEntity copy = new CustomFeatureEntity();
                    copy.classId = classId;
                    copy.name = original.name;
                    copy.description = original.description;
                    copy.type = original.type;
                    copy.customGainedAt = new ArrayList<>(original.customGainedAt);
                    copy.customTableData = new ArrayList<>(original.customTableData);
                    return copy;
                })
                .collect(Collectors.toList());

        repository.getCustomDao().insertFeatures(copies);
    }
}
