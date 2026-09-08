package com.murkfeatherstudio.questroll.feature_species.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.murkfeatherstudio.questroll.core.models.custom.custom_creature.CustomCreatureAction;
import com.murkfeatherstudio.questroll.core.models.custom.custom_species.CustomSpeciesDao;
import com.murkfeatherstudio.questroll.core.models.custom.custom_species.CustomSpeciesEntity;
import com.murkfeatherstudio.questroll.core.models.open5e.species.SpeciesDao;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

public class CustomSpeciesCreateViewModel extends ViewModel {

    public static final long NO_ID = -1L;

    private final CustomSpeciesDao customSpeciesDao;
    private final SpeciesDao speciesDao;
    private final long editId;
    private final Executor executor;

    private final MutableLiveData<CustomSpeciesEntity> editData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> saveResult = new MutableLiveData<>();

    private final MutableLiveData<List<AbilityBonus>> abilityBonuses = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<String>> languageKeys = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<CustomCreatureAction>> otherTraits = new MutableLiveData<>(new ArrayList<>());

    public CustomSpeciesCreateViewModel(CustomSpeciesDao customSpeciesDao,
                                        SpeciesDao speciesDao,
                                        long editId,
                                        Executor executor) {
        this.customSpeciesDao = customSpeciesDao;
        this.speciesDao = speciesDao;
        this.editId = editId;
        this.executor = executor;

        if (isEditMode()) loadExisting();
    }

    public boolean isEditMode() { return editId != NO_ID; }

    private void loadExisting() {
        customSpeciesDao.getById(editId).observeForever(s -> {
            if (s != null && editData.getValue() == null) {
                editData.setValue(s);
                Gson gson = new Gson();
                Type bonusType = new TypeToken<List<AbilityBonus>>(){}.getType();
                Type langType = new TypeToken<List<String>>(){}.getType();
                Type traitType = new TypeToken<List<CustomCreatureAction>>(){}.getType();
                try {
                    if (s.abilityBonusesJson != null && !s.abilityBonusesJson.isEmpty())
                        abilityBonuses.setValue(gson.fromJson(s.abilityBonusesJson, bonusType));
                    if (s.languageKeysJson != null && !s.languageKeysJson.isEmpty())
                        languageKeys.setValue(gson.fromJson(s.languageKeysJson, langType));
                    if (s.otherTraitsJson != null && !s.otherTraitsJson.isEmpty())
                        otherTraits.setValue(gson.fromJson(s.otherTraitsJson, traitType));
                } catch (Exception ignored) {}
            }
        });
    }

    public LiveData<CustomSpeciesEntity> getEditData() { return editData; }
    public LiveData<Boolean> getSaveResult() { return saveResult; }
    public LiveData<List<AbilityBonus>> getAbilityBonuses() { return abilityBonuses; }
    public LiveData<List<String>> getLanguageKeys() { return languageKeys; }
    public LiveData<List<CustomCreatureAction>> getOtherTraits() { return otherTraits; }

    public void addAbilityBonus(AbilityBonus bonus) {
        List<AbilityBonus> list = new ArrayList<>(abilityBonuses.getValue());
        list.add(bonus);
        abilityBonuses.setValue(list);
    }
    public void removeAbilityBonus(int position) {
        List<AbilityBonus> list = new ArrayList<>(abilityBonuses.getValue());
        if (position >= 0 && position < list.size()) list.remove(position);
        abilityBonuses.setValue(list);
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

    public void addOtherTrait(CustomCreatureAction trait) {
        List<CustomCreatureAction> list = new ArrayList<>(otherTraits.getValue());
        list.add(trait);
        otherTraits.setValue(list);
    }
    public void removeOtherTrait(int position) {
        List<CustomCreatureAction> list = new ArrayList<>(otherTraits.getValue());
        if (position >= 0 && position < list.size()) list.remove(position);
        otherTraits.setValue(list);
    }

    public void save(CustomSpeciesEntity entity) {
        executor.execute(() -> {
            try {
                Gson gson = new Gson();
                entity.abilityBonusesJson = gson.toJson(abilityBonuses.getValue());
                entity.languageKeysJson = gson.toJson(languageKeys.getValue());
                entity.otherTraitsJson = gson.toJson(otherTraits.getValue());

                if (isEditMode()) {
                    entity.id = editId;
                    customSpeciesDao.update(entity);
                } else {
                    if (customSpeciesDao.countByName(entity.name) > 0) {
                        saveResult.postValue(false);
                        return;
                    }
                    customSpeciesDao.insert(entity);
                }
                saveResult.postValue(true);
            } catch (Exception e) {
                saveResult.postValue(false);
            }
        });
    }

    public static class AbilityBonus {
        public String ability;
        public int bonus;
        public AbilityBonus(String ability, int bonus) {
            this.ability = ability;
            this.bonus = bonus;
        }
    }

    public static class Factory extends ViewModelProvider.NewInstanceFactory {
        private final CustomSpeciesDao customSpeciesDao;
        private final SpeciesDao speciesDao;
        private final long editId;
        private final Executor executor;

        public Factory(CustomSpeciesDao customSpeciesDao, SpeciesDao speciesDao,
                       long editId, Executor executor) {
            this.customSpeciesDao = customSpeciesDao;
            this.speciesDao = speciesDao;
            this.editId = editId;
            this.executor = executor;
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            return (T) new CustomSpeciesCreateViewModel(customSpeciesDao, speciesDao, editId, executor);
        }
    }
}