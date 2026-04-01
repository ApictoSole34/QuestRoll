package com.fizzycoyote.qusetroll.feature_alignment.view_model;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.fizzycoyote.qusetroll.core.models.custom.custom_alignment.CustomAlignmentDao;
import com.fizzycoyote.qusetroll.core.models.custom.custom_alignment.CustomAlignmentEntity;

import java.util.concurrent.Executor;

public class CustomAlignmentCreateViewModel extends ViewModel {
    private final CustomAlignmentDao dao;
    private final long editId;
    private final Executor executor;

    private final MutableLiveData<CustomAlignmentEntity> editData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> saveResult = new MutableLiveData<>();

    public CustomAlignmentCreateViewModel(CustomAlignmentDao dao, long editId, Executor executor) {
        this.dao = dao;
        this.editId = editId;
        this.executor = executor;
        if (isEditMode()) loadExisting();
    }

    public boolean isEditMode() { return editId != -1; }

    private void loadExisting() {
        dao.getById(editId).observeForever(alignment -> {
            if (alignment != null && editData.getValue() == null) {
                editData.setValue(alignment);
            }
        });
    }

    public LiveData<CustomAlignmentEntity> getEditData() { return editData; }
    public LiveData<Boolean> getSaveResult() { return saveResult; }

    public void save(CustomAlignmentEntity alignment) {
        executor.execute(() -> {
            try {
                if (isEditMode()) {
                    alignment.id = editId;
                    dao.update(alignment);
                } else {
                    if (dao.countByName(alignment.name) > 0) {
                        saveResult.postValue(false);
                        return;
                    }
                    dao.insert(alignment);
                }
                saveResult.postValue(true);
            } catch (Exception e) {
                saveResult.postValue(false);
            }
        });
    }

    public static class Factory extends ViewModelProvider.NewInstanceFactory {
        private final CustomAlignmentDao dao;
        private final long editId;
        private final Executor executor;

        public Factory(CustomAlignmentDao dao, long editId, Executor executor) {
            this.dao = dao; this.editId = editId; this.executor = executor;
        }

        @NonNull @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            return (T) new CustomAlignmentCreateViewModel(dao, editId, executor);
        }
    }
}