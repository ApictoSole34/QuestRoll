package com.fizzycoyote.qusetroll.feature_environment.view_model;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.fizzycoyote.qusetroll.core.models.custom.custom_environment.CustomEnvironmentDao;
import com.fizzycoyote.qusetroll.core.models.custom.custom_environment.CustomEnvironmentEntity;

import java.util.concurrent.Executor;

public class CustomEnvironmentCreateViewModel extends ViewModel {
    private final CustomEnvironmentDao dao;
    private final long editId;
    private final Executor executor;
    private final MutableLiveData<CustomEnvironmentEntity> editData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> saveResult = new MutableLiveData<>();

    public CustomEnvironmentCreateViewModel(CustomEnvironmentDao dao, long editId, Executor executor) {
        this.dao = dao; this.editId = editId; this.executor = executor;
        if (isEditMode()) loadExisting();
    }
    public boolean isEditMode() { return editId != -1; }
    private void loadExisting() {
        dao.getById(editId).observeForever(env -> {
            if (env != null && editData.getValue() == null) editData.setValue(env);
        });
    }
    public LiveData<CustomEnvironmentEntity> getEditData() { return editData; }
    public LiveData<Boolean> getSaveResult() { return saveResult; }
    public void save(CustomEnvironmentEntity env) {
        executor.execute(() -> {
            try {
                if (isEditMode()) { env.id = editId; dao.update(env); }
                else { if (dao.countByName(env.name) > 0) { saveResult.postValue(false); return; } dao.insert(env); }
                saveResult.postValue(true);
            } catch (Exception e) { saveResult.postValue(false); }
        });
    }
    public static class Factory extends ViewModelProvider.NewInstanceFactory {
        private final CustomEnvironmentDao dao; private final long editId; private final Executor executor;
        public Factory(CustomEnvironmentDao dao, long editId, Executor executor) { this.dao = dao; this.editId = editId; this.executor = executor; }
        @NonNull @Override public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            return (T) new CustomEnvironmentCreateViewModel(dao, editId, executor);
        }
    }
}