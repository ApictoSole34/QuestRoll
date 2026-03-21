package com.fizzycoyote.qusetroll.feature_species.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.fizzycoyote.qusetroll.core.models.custom.custom_creature.CustomCreatureAction;
import com.fizzycoyote.qusetroll.core.models.custom.custom_species.CustomSpeciesDao;
import com.fizzycoyote.qusetroll.core.models.custom.custom_species.CustomSpeciesEntity;
import com.fizzycoyote.qusetroll.core.models.open5e.species.SpeciesDao;
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
    private final MutableLiveData<List<CustomCreatureAction>> traits =
            new MutableLiveData<>(new ArrayList<>());

    private final MutableLiveData<List<ParentSpeciesOption>> parentOptions = new MutableLiveData<>();

    public CustomSpeciesCreateViewModel(CustomSpeciesDao customSpeciesDao,
                                        SpeciesDao speciesDao,
                                        long editId,
                                        Executor executor) {
        this.customSpeciesDao = customSpeciesDao;
        this.speciesDao = speciesDao;
        this.editId = editId;
        this.executor = executor;

        loadParentOptions();
        if (isEditMode()) loadExisting();
    }

    public boolean isEditMode() { return editId != NO_ID; }

    private void loadParentOptions() {
        executor.execute(() -> {
            List<ParentSpeciesOption> options = new ArrayList<>();
            options.add(new ParentSpeciesOption("", "", "None (main species)"));

            parentOptions.postValue(options);
        });
    }

    private void loadExisting() {
        customSpeciesDao.getById(editId).observeForever(s -> {
            if (s != null && editData.getValue() == null) {
                editData.setValue(s);
                Gson gson = new Gson();
                Type type = new TypeToken<List<CustomCreatureAction>>(){}.getType();
                try {
                    if (s.traitsJson != null && !s.traitsJson.isEmpty())
                        traits.setValue(gson.fromJson(s.traitsJson, type));
                } catch (Exception ignored) {}
            }
        });
    }

    public LiveData<CustomSpeciesEntity> getEditData() { return editData; }
    public LiveData<Boolean> getSaveResult() { return saveResult; }
    public LiveData<List<CustomCreatureAction>> getTraits() { return traits; }
    public LiveData<List<ParentSpeciesOption>> getParentOptions() { return parentOptions; }

    public void addTrait(CustomCreatureAction t) {
        List<CustomCreatureAction> list = new ArrayList<>(
                traits.getValue() != null ? traits.getValue() : new ArrayList<>());
        list.add(t); traits.setValue(list);
    }

    public void removeTrait(int i) {
        List<CustomCreatureAction> list = new ArrayList<>(
                traits.getValue() != null ? traits.getValue() : new ArrayList<>());
        if (i >= 0 && i < list.size()) { list.remove(i); traits.setValue(list); }
    }

    public void save(CustomSpeciesEntity entity) {
        executor.execute(() -> {
            try {
                entity.traitsJson = new Gson().toJson(traits.getValue());
                if (isEditMode()) {
                    entity.id = editId;
                    customSpeciesDao.update(entity);
                } else {
                    if (customSpeciesDao.countByName(entity.name) > 0) {
                        saveResult.postValue(false); return;
                    }
                    customSpeciesDao.insert(entity);
                }
                saveResult.postValue(true);
            } catch (Exception e) {
                saveResult.postValue(false);
            }
        });
    }

    public static class ParentSpeciesOption {
        public final String key;
        public final String displayName;
        public final String label;

        public ParentSpeciesOption(String key, String displayName, String label) {
            this.key = key;
            this.displayName = displayName;
            this.label = label;
        }

        @Override
        public String toString() { return label; }
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