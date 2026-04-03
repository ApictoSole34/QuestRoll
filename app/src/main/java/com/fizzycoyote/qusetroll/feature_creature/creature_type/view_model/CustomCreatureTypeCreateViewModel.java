package com.fizzycoyote.qusetroll.feature_creature.creature_type.view_model;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.fizzycoyote.qusetroll.core.models.custom.custom_creature_type.CustomCreatureTypeDao;
import com.fizzycoyote.qusetroll.core.models.custom.custom_creature_type.CustomCreatureTypeEntity;

import java.util.concurrent.Executor;

public class CustomCreatureTypeCreateViewModel extends ViewModel {
    private final CustomCreatureTypeDao dao;
    private final long editId;
    private final Executor executor;
    private final MutableLiveData<CustomCreatureTypeEntity> editData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> saveResult = new MutableLiveData<>();

    public CustomCreatureTypeCreateViewModel(CustomCreatureTypeDao dao, long editId, Executor executor) {
        this.dao = dao;
        this.editId = editId;
        this.executor = executor;
        if (isEditMode()) loadExisting();
    }

    public boolean isEditMode() { return editId != -1; }

    private void loadExisting() {
        dao.getById(editId).observeForever(type -> {
            if (type != null && editData.getValue() == null) {
                editData.setValue(type);
            }
        });
    }

    public LiveData<CustomCreatureTypeEntity> getEditData() { return editData; }
    public LiveData<Boolean> getSaveResult() { return saveResult; }

    public void save(CustomCreatureTypeEntity type) {
        executor.execute(() -> {
            try {
                if (isEditMode()) {
                    type.id = editId;
                    dao.update(type);
                } else {
                    if (dao.countByName(type.name) > 0) {
                        saveResult.postValue(false);
                        return;
                    }
                    dao.insert(type);
                }
                saveResult.postValue(true);
            } catch (Exception e) {
                saveResult.postValue(false);
            }
        });
    }

    public static class Factory extends ViewModelProvider.NewInstanceFactory {
        private final CustomCreatureTypeDao dao;
        private final long editId;
        private final Executor executor;
        public Factory(CustomCreatureTypeDao dao, long editId, Executor executor) {
            this.dao = dao; this.editId = editId; this.executor = executor;
        }
        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            return (T) new CustomCreatureTypeCreateViewModel(dao, editId, executor);
        }
    }
}