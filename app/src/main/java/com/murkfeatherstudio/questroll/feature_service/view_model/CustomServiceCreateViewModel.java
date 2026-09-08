package com.murkfeatherstudio.questroll.feature_service.view_model;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.murkfeatherstudio.questroll.core.models.custom.custom_service.CustomServiceDao;
import com.murkfeatherstudio.questroll.core.models.custom.custom_service.CustomServiceEntity;

import java.util.concurrent.Executor;

public class CustomServiceCreateViewModel extends ViewModel {
    private final CustomServiceDao dao;
    private final long editId;
    private final Executor executor;

    private final MutableLiveData<CustomServiceEntity> editData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> saveResult = new MutableLiveData<>();

    public CustomServiceCreateViewModel(CustomServiceDao dao, long editId, Executor executor) {
        this.dao = dao;
        this.editId = editId;
        this.executor = executor;
        if (isEditMode()) loadExisting();
    }

    public boolean isEditMode() { return editId != -1; }

    private void loadExisting() {
        dao.getById(editId).observeForever(service -> {
            if (service != null && editData.getValue() == null) {
                editData.setValue(service);
            }
        });
    }

    public LiveData<CustomServiceEntity> getEditData() { return editData; }
    public LiveData<Boolean> getSaveResult() { return saveResult; }

    public void save(CustomServiceEntity service) {
        executor.execute(() -> {
            try {
                if (isEditMode()) {
                    service.id = editId;
                    dao.update(service);
                } else {
                    if (dao.countByName(service.name) > 0) {
                        saveResult.postValue(false);
                        return;
                    }
                    dao.insert(service);
                }
                saveResult.postValue(true);
            } catch (Exception e) {
                saveResult.postValue(false);
            }
        });
    }

    public static class Factory extends ViewModelProvider.NewInstanceFactory {
        private final CustomServiceDao dao;
        private final long editId;
        private final Executor executor;
        public Factory(CustomServiceDao dao, long editId, Executor executor) {
            this.dao = dao; this.editId = editId; this.executor = executor;
        }
        @NonNull @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            return (T) new CustomServiceCreateViewModel(dao, editId, executor);
        }
    }
}
