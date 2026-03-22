package com.fizzycoyote.qusetroll.feature_item.view_model.weapon;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.fizzycoyote.qusetroll.core.models.custom.custom_creature.CustomCreatureAction;
import com.fizzycoyote.qusetroll.core.models.custom.custom_weapon.CustomWeaponDao;
import com.fizzycoyote.qusetroll.core.models.custom.custom_weapon.CustomWeaponEntity;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

public class CustomWeaponCreateViewModel extends ViewModel {

    public static final long NO_ID = -1L;

    private final CustomWeaponDao dao;
    private final long editId;
    private final Executor executor;

    private final MutableLiveData<CustomWeaponEntity> editData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> saveResult = new MutableLiveData<>();
    private final MutableLiveData<List<CustomCreatureAction>> properties =
            new MutableLiveData<>(new ArrayList<>());

    public CustomWeaponCreateViewModel(CustomWeaponDao dao, long editId, Executor executor) {
        this.dao = dao;
        this.editId = editId;
        this.executor = executor;
        if (isEditMode()) loadExisting();
    }

    public boolean isEditMode() { return editId != NO_ID; }

    private void loadExisting() {
        dao.getById(editId).observeForever(w -> {
            if (w != null && editData.getValue() == null) {
                editData.setValue(w);
                try {
                    Type type = new TypeToken<List<CustomCreatureAction>>(){}.getType();
                    List<CustomCreatureAction> list = new Gson().fromJson(w.propertiesJson, type);
                    properties.setValue(list != null ? list : new ArrayList<>());
                } catch (Exception ignored) {}
            }
        });
    }

    public LiveData<CustomWeaponEntity> getEditData() { return editData; }
    public LiveData<Boolean> getSaveResult() { return saveResult; }
    public LiveData<List<CustomCreatureAction>> getProperties() { return properties; }

    public void addProperty(String name, String desc) {
        CustomCreatureAction p = new CustomCreatureAction();
        p.name = name; p.desc = desc;
        List<CustomCreatureAction> list = new ArrayList<>(
                properties.getValue() != null ? properties.getValue() : new ArrayList<>());
        list.add(p); properties.setValue(list);
    }

    public void removeProperty(int i) {
        List<CustomCreatureAction> list = new ArrayList<>(
                properties.getValue() != null ? properties.getValue() : new ArrayList<>());
        if (i >= 0 && i < list.size()) { list.remove(i); properties.setValue(list); }
    }

    public void save(CustomWeaponEntity entity) {
        executor.execute(() -> {
            try {
                entity.propertiesJson = new Gson().toJson(properties.getValue());
                if (isEditMode()) {
                    entity.id = editId;
                    dao.update(entity);
                } else {
                    if (dao.countByName(entity.name) > 0) { saveResult.postValue(false); return; }
                    dao.insert(entity);
                }
                saveResult.postValue(true);
            } catch (Exception e) { saveResult.postValue(false); }
        });
    }

    public static class Factory extends ViewModelProvider.NewInstanceFactory {
        private final CustomWeaponDao dao;
        private final long editId;
        private final Executor executor;

        public Factory(CustomWeaponDao dao, long editId, Executor executor) {
            this.dao = dao; this.editId = editId; this.executor = executor;
        }

        @NonNull @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            return (T) new CustomWeaponCreateViewModel(dao, editId, executor);
        }
    }
}
