package com.fizzycoyote.qusetroll.feature_creature.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.fizzycoyote.qusetroll.core.models.custom.custom_creature_type.CustomCreatureTypeDao;
import com.fizzycoyote.qusetroll.core.models.custom.custom_creature_type.CustomCreatureTypeEntity;
import com.fizzycoyote.qusetroll.core.models.open5e.creature_type.CreatureTypeDao;
import com.fizzycoyote.qusetroll.core.models.open5e.creature_type.CreatureTypeEntity;
import com.fizzycoyote.qusetroll.core.models.custom.custom_creature.CustomCreatureAction;
import com.fizzycoyote.qusetroll.core.models.custom.custom_creature.CustomCreatureDao;
import com.fizzycoyote.qusetroll.core.models.custom.custom_creature.CustomCreatureEntity;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executor;

public class CustomCreatureCreateViewModel extends ViewModel {

    public static final long NO_ID = -1L;

    private final CustomCreatureDao creatureDao;
    private final CustomCreatureTypeDao customTypeDao;
    private final CreatureTypeDao apiTypeDao;
    private final long editId;
    private final Executor executor;

    private final MutableLiveData<CustomCreatureEntity> editData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> saveResult = new MutableLiveData<>();
    private final LiveData<List<String>> allTypeNames;

    private final MutableLiveData<List<CustomCreatureAction>> actions =
            new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<CustomCreatureAction>> traits =
            new MutableLiveData<>(new ArrayList<>());

    public CustomCreatureCreateViewModel(CustomCreatureDao creatureDao,
                                         CustomCreatureTypeDao customTypeDao,
                                         CreatureTypeDao apiTypeDao,
                                         long editId,
                                         Executor executor) {
        this.creatureDao = creatureDao;
        this.customTypeDao = customTypeDao;
        this.apiTypeDao = apiTypeDao;
        this.editId = editId;
        this.executor = executor;

        LiveData<List<CreatureTypeEntity>> apiTypesLive = apiTypeDao.getAll();
        LiveData<List<CustomCreatureTypeEntity>> customTypesLive = customTypeDao.getAll();

        MediatorLiveData<List<String>> typeMediator = new MediatorLiveData<>();
        typeMediator.addSource(apiTypesLive, apiTypes ->
                mergeTypeNames(apiTypes, customTypesLive.getValue(), typeMediator));
        typeMediator.addSource(customTypesLive, customTypes ->
                mergeTypeNames(apiTypesLive.getValue(), customTypes, typeMediator));
        allTypeNames = typeMediator;

        if (isEditMode()) loadExisting();
    }

    private void mergeTypeNames(List<CreatureTypeEntity> apiTypes,
                                List<CustomCreatureTypeEntity> customTypes,
                                MediatorLiveData<List<String>> out) {
        List<String> names = new ArrayList<>();
        if (apiTypes != null)
            for (CreatureTypeEntity t : apiTypes)
                if (!names.contains(t.name)) names.add(t.name);
        if (customTypes != null)
            for (CustomCreatureTypeEntity t : customTypes)
                if (!names.contains(t.name)) names.add(t.name);
        Collections.sort(names);
        out.setValue(names);
    }

    public boolean isEditMode() { return editId != NO_ID; }

    private void loadExisting() {
        creatureDao.getById(editId).observeForever(c -> {
            if (c != null && editData.getValue() == null) {
                editData.setValue(c);
                Gson gson = new Gson();
                Type listType = new TypeToken<List<CustomCreatureAction>>(){}.getType();
                try {
                    if (c.actionsJson != null && !c.actionsJson.isEmpty())
                        actions.setValue(gson.fromJson(c.actionsJson, listType));
                } catch (Exception ignored) {}
                try {
                    if (c.traitsJson != null && !c.traitsJson.isEmpty())
                        traits.setValue(gson.fromJson(c.traitsJson, listType));
                } catch (Exception ignored) {}
            }
        });
    }

    public LiveData<CustomCreatureEntity> getEditData() { return editData; }
    public LiveData<Boolean> getSaveResult() { return saveResult; }
    public LiveData<List<String>> getAllTypeNames() { return allTypeNames; }
    public LiveData<List<CustomCreatureAction>> getActions() { return actions; }
    public LiveData<List<CustomCreatureAction>> getTraits() { return traits; }

    public void addAction(CustomCreatureAction a) {
        List<CustomCreatureAction> list = new ArrayList<>(actions.getValue());
        list.add(a);
        actions.setValue(list);
    }

    public void removeAction(int i) {
        List<CustomCreatureAction> list = new ArrayList<>(actions.getValue());
        if (i >= 0 && i < list.size()) list.remove(i);
        actions.setValue(list);
    }

    public void addTrait(CustomCreatureAction t) {
        List<CustomCreatureAction> list = new ArrayList<>(traits.getValue());
        list.add(t);
        traits.setValue(list);
    }

    public void removeTrait(int i) {
        List<CustomCreatureAction> list = new ArrayList<>(traits.getValue());
        if (i >= 0 && i < list.size()) list.remove(i);
        traits.setValue(list);
    }

    public void save(CustomCreatureEntity entity) {
        executor.execute(() -> {
            try {
                Gson gson = new Gson();
                entity.actionsJson = gson.toJson(actions.getValue());
                entity.traitsJson = gson.toJson(traits.getValue());

                int wisMod = (entity.wis - 10) / 2;
                entity.passivePerception = 10 + wisMod;
                entity.initiativeBonus = (entity.dex - 10) / 2;

                if (isEditMode()) {
                    entity.id = editId;
                    creatureDao.update(entity);
                } else {
                    if (creatureDao.countByName(entity.name) > 0) {
                        saveResult.postValue(false);
                        return;
                    }
                    creatureDao.insert(entity);
                }
                saveResult.postValue(true);
            } catch (Exception e) {
                saveResult.postValue(false);
            }
        });
    }

    public static class Factory extends ViewModelProvider.NewInstanceFactory {
        private final CustomCreatureDao creatureDao;
        private final CustomCreatureTypeDao customTypeDao;
        private final CreatureTypeDao apiTypeDao;
        private final long editId;
        private final Executor executor;

        public Factory(CustomCreatureDao creatureDao,
                       CustomCreatureTypeDao customTypeDao,
                       CreatureTypeDao apiTypeDao,
                       long editId,
                       Executor executor) {
            this.creatureDao = creatureDao;
            this.customTypeDao = customTypeDao;
            this.apiTypeDao = apiTypeDao;
            this.editId = editId;
            this.executor = executor;
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            return (T) new CustomCreatureCreateViewModel(creatureDao, customTypeDao, apiTypeDao, editId, executor);
        }
    }
}