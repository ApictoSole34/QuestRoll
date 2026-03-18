package com.fizzycoyote.qusetroll.feature_spell.view_model;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.fizzycoyote.qusetroll.core.models.custom.custom_spell.CustomCastingOption;
import com.fizzycoyote.qusetroll.core.models.custom.custom_spell.CustomSpellDao;
import com.fizzycoyote.qusetroll.core.models.custom.custom_spell.CustomSpellEntity;
import com.fizzycoyote.qusetroll.core.models.custom.custom_spell.CustomSpellSchoolDao;
import com.fizzycoyote.qusetroll.core.models.custom.custom_spell.CustomSpellSchoolEntity;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

public class CustomSpellCreateViewModel extends ViewModel {

    public static final long NO_ID = -1L;

    private final CustomSpellDao spellDao;
    private final CustomSpellSchoolDao schoolDao;
    private final long editSpellId;
    private final Executor executor;

    private final MutableLiveData<CustomSpellEntity> editData = new MutableLiveData<>();
    private final MutableLiveData<List<CustomSpellSchoolEntity>> customSchools = new MutableLiveData<>();
    private final MutableLiveData<Boolean> saveResult = new MutableLiveData<>();

    private final MutableLiveData<List<CustomCastingOption>> castingOptions =
            new MutableLiveData<>(new ArrayList<>());

    public CustomSpellCreateViewModel(CustomSpellDao spellDao,
                                      CustomSpellSchoolDao schoolDao,
                                      long editSpellId,
                                      Executor executor) {
        this.spellDao = spellDao;
        this.schoolDao = schoolDao;
        this.editSpellId = editSpellId;
        this.executor = executor;

        loadSchools();
        if (isEditMode()) loadExistingSpell();
    }

    public boolean isEditMode() { return editSpellId != NO_ID; }

    private void loadExistingSpell() {
        spellDao.getById(editSpellId).observeForever(spell -> {
            if (spell != null && editData.getValue() == null) {
                editData.setValue(spell);
                if (spell.castingOptionsJson != null && !spell.castingOptionsJson.isEmpty()) {
                    try {
                        Gson gson = new Gson();
                        Type type = new TypeToken<List<CustomCastingOption>>(){}.getType();
                        List<CustomCastingOption> options = gson.fromJson(spell.castingOptionsJson, type);
                        castingOptions.setValue(options != null ? options : new ArrayList<>());
                    } catch (Exception e) {
                        castingOptions.setValue(new ArrayList<>());
                    }
                }
            }
        });
    }

    private void loadSchools() {
        schoolDao.getAll().observeForever(schools ->
                customSchools.postValue(schools));
    }

    public LiveData<CustomSpellEntity> getEditData() { return editData; }
    public LiveData<List<CustomSpellSchoolEntity>> getCustomSchools() { return customSchools; }
    public LiveData<Boolean> getSaveResult() { return saveResult; }
    public LiveData<List<CustomCastingOption>> getCastingOptions() { return castingOptions; }

    public void addCastingOption(CustomCastingOption option) {
        List<CustomCastingOption> current = new ArrayList<>(
                castingOptions.getValue() != null ? castingOptions.getValue() : new ArrayList<>());
        current.add(option);
        castingOptions.setValue(current);
    }

    public void removeCastingOption(int index) {
        List<CustomCastingOption> current = new ArrayList<>(
                castingOptions.getValue() != null ? castingOptions.getValue() : new ArrayList<>());
        if (index >= 0 && index < current.size()) {
            current.remove(index);
            castingOptions.setValue(current);
        }
    }

    public void saveSpell(CustomSpellEntity entity) {
        executor.execute(() -> {
            try {
                List<CustomCastingOption> options = castingOptions.getValue();
                entity.castingOptionsJson = options != null && !options.isEmpty()
                        ? new Gson().toJson(options) : "";

                if (isEditMode()) {
                    entity.id = editSpellId;
                    spellDao.update(entity);
                } else {
                    if (spellDao.countByName(entity.name) > 0) {
                        saveResult.postValue(false);
                        return;
                    }
                    spellDao.insert(entity);
                }
                saveResult.postValue(true);
            } catch (Exception e) {
                saveResult.postValue(false);
            }
        });
    }

    public void saveCustomSchool(String name, String description) {
        executor.execute(() -> {
            if (schoolDao.countByName(name) > 0) return;
            CustomSpellSchoolEntity school = new CustomSpellSchoolEntity();
            school.name = name;
            school.description = description;
            schoolDao.insert(school);
        });
    }

    public void deleteCustomSchool(long id) {
        executor.execute(() -> schoolDao.delete(id));
    }

    public static class Factory extends ViewModelProvider.NewInstanceFactory {
        private final CustomSpellDao spellDao;
        private final CustomSpellSchoolDao schoolDao;
        private final long editSpellId;
        private final Executor executor;

        public Factory(CustomSpellDao spellDao, CustomSpellSchoolDao schoolDao,
                       long editSpellId, Executor executor) {
            this.spellDao = spellDao;
            this.schoolDao = schoolDao;
            this.editSpellId = editSpellId;
            this.executor = executor;
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            return (T) new CustomSpellCreateViewModel(spellDao, schoolDao, editSpellId, executor);
        }
    }
}