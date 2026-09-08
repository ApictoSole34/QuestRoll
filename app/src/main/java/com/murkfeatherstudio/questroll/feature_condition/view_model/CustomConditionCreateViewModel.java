package com.murkfeatherstudio.questroll.feature_condition.view_model;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.murkfeatherstudio.questroll.core.models.custom.custom_condition.CustomConditionDao;
import com.murkfeatherstudio.questroll.core.models.custom.custom_condition.CustomConditionEntity;

import java.util.concurrent.Executor;

public class CustomConditionCreateViewModel extends ViewModel {
    private final CustomConditionDao dao;
    private final long editId;
    private final Executor executor;
    private final MutableLiveData<CustomConditionEntity> editData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> saveResult = new MutableLiveData<>();

    public CustomConditionCreateViewModel(CustomConditionDao dao, long editId, Executor executor) {
        this.dao = dao;
        this.editId = editId;
        this.executor = executor;
        if (isEditMode()) loadExisting();
    }

    public boolean isEditMode() { return editId != -1; }

    private void loadExisting() {
        dao.getById(editId).observeForever(condition -> {
            if (condition != null && editData.getValue() == null) {
                editData.setValue(condition);
            }
        });
    }

    public LiveData<CustomConditionEntity> getEditData() { return editData; }
    public LiveData<Boolean> getSaveResult() { return saveResult; }

    public void save(CustomConditionEntity condition) {
        executor.execute(() -> {
            try {
                if (isEditMode()) {
                    condition.id = editId;
                    dao.update(condition);
                } else {
                    if (dao.countByName(condition.name) > 0) {
                        saveResult.postValue(false);
                        return;
                    }
                    dao.insert(condition);
                }
                saveResult.postValue(true);
            } catch (Exception e) {
                saveResult.postValue(false);
            }
        });
    }

    public static class Factory extends ViewModelProvider.NewInstanceFactory {
        private final CustomConditionDao dao;
        private final long editId;
        private final Executor executor;
        public Factory(CustomConditionDao dao, long editId, Executor executor) {
            this.dao = dao; this.editId = editId; this.executor = executor;
        }
        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            return (T) new CustomConditionCreateViewModel(dao, editId, executor);
        }
    }
}