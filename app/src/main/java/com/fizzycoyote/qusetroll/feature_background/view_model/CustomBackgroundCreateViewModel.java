package com.fizzycoyote.qusetroll.feature_background.view_model;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.fizzycoyote.qusetroll.core.models.character.CharacterCreationDTO;
import com.fizzycoyote.qusetroll.core.models.character.CharacterTraitEntity;
import com.fizzycoyote.qusetroll.core.models.custom.custom_background.CustomBackgroundDao;
import com.fizzycoyote.qusetroll.core.models.custom.custom_background.CustomBackgroundEntity;
import com.fizzycoyote.qusetroll.core.models.open5e.background.BackgroundDto;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

public class CustomBackgroundCreateViewModel extends ViewModel {

    public static final long NO_ID = -1L;

    private final CustomBackgroundDao dao;
    private final long editId;
    private final Executor executor;
    private final MutableLiveData<CustomBackgroundEntity> editData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> saveResult = new MutableLiveData<>();

    private final List<String> equipment = new ArrayList<>();
    private final List<String> languages = new ArrayList<>();
    private final List<String> skills = new ArrayList<>();
    private final List<String> tools = new ArrayList<>();
    private final List<CharacterTraitEntity> features = new ArrayList<>();

    private String equipmentDescription = "";
    private String languagesDescription = "";
    private int languageChoices = 0;

    public CustomBackgroundCreateViewModel(CustomBackgroundDao dao, long editId, Executor executor) {
        this.dao = dao;
        this.editId = editId;
        this.executor = executor;
        if (isEditMode()) loadExisting();
    }

    public boolean isEditMode() { return editId != NO_ID; }

    private void loadExisting() {
        dao.getById(editId).observeForever(entity -> {
            if (entity != null && editData.getValue() == null) {
                editData.setValue(entity);
                if (entity.equipmentJson != null) {
                    Type type = new TypeToken<List<String>>(){}.getType();
                    equipment.addAll(new Gson().fromJson(entity.equipmentJson, type));
                }
                if (entity.languagesJson != null) {
                    Type type = new TypeToken<List<String>>(){}.getType();
                    languages.addAll(new Gson().fromJson(entity.languagesJson, type));
                }
                if (entity.skillProficienciesJson != null) {
                    Type type = new TypeToken<List<String>>(){}.getType();
                    skills.addAll(new Gson().fromJson(entity.skillProficienciesJson, type));
                }
                if (entity.toolProficienciesJson != null) {
                    Type type = new TypeToken<List<String>>(){}.getType();
                    tools.addAll(new Gson().fromJson(entity.toolProficienciesJson, type));
                }
                if (entity.featuresJson != null) {
                    Type type = new TypeToken<List<CharacterTraitEntity>>(){}.getType();
                    features.addAll(new Gson().fromJson(entity.featuresJson, type));
                }
                equipmentDescription = entity.equipmentDescription != null ? entity.equipmentDescription : "";
                languagesDescription = entity.languagesDescription != null ? entity.languagesDescription : "";
                languageChoices = entity.languageChoices;   // NOWE
            }
        });
    }

    public LiveData<CustomBackgroundEntity> getEditData() { return editData; }
    public LiveData<Boolean> getSaveResult() { return saveResult; }

    public List<String> getEquipmentItems() { return equipment; }
    public List<String> getLanguageItems() { return languages; }
    public List<String> getSkillItems() { return skills; }
    public List<String> getToolItems() { return tools; }
    public List<CharacterTraitEntity> getFeatureItems() { return features; }
    public String getEquipmentDescription() { return equipmentDescription; }
    public String getLanguagesDescription() { return languagesDescription; }
    public int getLanguageChoices() { return languageChoices; }   // NOWE

    public void setEquipmentDescription(String desc) { this.equipmentDescription = desc; }
    public void setLanguagesDescription(String desc) { this.languagesDescription = desc; }
    public void setLanguageChoices(int choices) { this.languageChoices = choices; }   // NOWE

    public void addEquipmentItem(String itemName) { equipment.add(itemName); }
    public void addLanguage(String lang) { languages.add(lang); }
    public void addSkill(String skill) { skills.add(skill); }
    public void addTool(String tool) { tools.add(tool); }
    public void addFeature(CharacterTraitEntity feature) { features.add(feature); }

    public void removeEquipmentItem(String itemName) { equipment.remove(itemName); }
    public void removeLanguage(String lang) { languages.remove(lang); }
    public void removeSkill(String skill) { skills.remove(skill); }
    public void removeTool(String tool) { tools.remove(tool); }
    public void removeFeature(CharacterTraitEntity feature) { features.remove(feature); }

    public void save(String name, String desc, String gameSystem, int startingGold,
                     String equipmentDescription, String languagesDescription, int languageChoices) {
        executor.execute(() -> {
            try {
                CustomBackgroundEntity e = new CustomBackgroundEntity();
                if (isEditMode()) {
                    e = editData.getValue();
                    if (e == null) return;
                } else {
                    e.key = "custom_bg_" + System.currentTimeMillis();
                }
                e.name = name;
                e.desc = desc;
                e.gameSystem = gameSystem;
                e.startingGold = startingGold;
                e.equipmentJson = new Gson().toJson(equipment);
                e.equipmentDescription = equipmentDescription;
                e.languagesJson = new Gson().toJson(languages);
                e.languagesDescription = languagesDescription;
                e.languageChoices = languageChoices;
                e.skillProficienciesJson = new Gson().toJson(skills);
                e.toolProficienciesJson = new Gson().toJson(tools);
                e.featuresJson = new Gson().toJson(features);

                if (isEditMode()) {
                    dao.update(e);
                } else {
                    if (dao.countByName(name) > 0) {
                        saveResult.postValue(false);
                        return;
                    }
                    dao.insert(e);
                }
                saveResult.postValue(true);
            } catch (Exception ex) {
                saveResult.postValue(false);
            }
        });
    }

    public static class Factory extends ViewModelProvider.NewInstanceFactory {
        private final CustomBackgroundDao dao;
        private final long editId;
        private final Executor executor;
        public Factory(CustomBackgroundDao dao, long editId, Executor executor) {
            this.dao = dao; this.editId = editId; this.executor = executor;
        }
        @NonNull @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            return (T) new CustomBackgroundCreateViewModel(dao, editId, executor);
        }
    }
}