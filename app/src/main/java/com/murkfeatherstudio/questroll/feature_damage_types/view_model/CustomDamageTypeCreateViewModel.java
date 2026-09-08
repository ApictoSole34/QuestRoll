package com.murkfeatherstudio.questroll.feature_damage_types.view_model;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.murkfeatherstudio.questroll.core.models.custom.custom_damage_types.CustomDamageTypeDao;
import com.murkfeatherstudio.questroll.core.models.custom.custom_damage_types.CustomDamageTypeEntity;

import java.util.concurrent.Executor;

public class CustomDamageTypeCreateViewModel extends ViewModel {
    private final CustomDamageTypeDao dao;
    private final long editId;
    private final Executor executor;
    private final MutableLiveData<CustomDamageTypeEntity> editData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> saveResult = new MutableLiveData<>();

    public CustomDamageTypeCreateViewModel(CustomDamageTypeDao dao, long editId, Executor executor) {
        this.dao = dao;
        this.editId = editId;
        this.executor = executor;
        if (isEditMode()) loadExisting();
    }

    public boolean isEditMode() { return editId != -1; }

    private void loadExisting() {
        dao.getById(editId).observeForever(type -> {
            if (type != null && editData.getValue() == null) editData.setValue(type);
        });
    }

    public LiveData<CustomDamageTypeEntity> getEditData() { return editData; }
    public LiveData<Boolean> getSaveResult() { return saveResult; }

    public void save(CustomDamageTypeEntity type) {
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
        private final CustomDamageTypeDao dao;
        private final long editId;
        private final Executor executor;
        public Factory(CustomDamageTypeDao dao, long editId, Executor executor) {
            this.dao = dao; this.editId = editId; this.executor = executor;
        }
        @NonNull @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            return (T) new CustomDamageTypeCreateViewModel(dao, editId, executor);
        }
    }
}