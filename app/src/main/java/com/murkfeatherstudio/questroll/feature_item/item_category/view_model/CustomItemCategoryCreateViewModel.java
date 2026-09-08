package com.murkfeatherstudio.questroll.feature_item.item_category.view_model;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import com.murkfeatherstudio.questroll.core.models.custom.custom_item_category.CustomItemCategoryDao;
import com.murkfeatherstudio.questroll.core.models.custom.custom_item_category.CustomItemCategoryEntity;
import java.util.concurrent.Executor;

public class CustomItemCategoryCreateViewModel extends ViewModel {
    private final CustomItemCategoryDao dao;
    private final long editId;
    private final Executor executor;
    private final MutableLiveData<CustomItemCategoryEntity> editData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> saveResult = new MutableLiveData<>();

    public CustomItemCategoryCreateViewModel(CustomItemCategoryDao dao, long editId, Executor executor) {
        this.dao = dao;
        this.editId = editId;
        this.executor = executor;
        if (isEditMode()) loadExisting();
    }

    public boolean isEditMode() { return editId != -1; }

    private void loadExisting() {
        dao.getById(editId).observeForever(cat -> {
            if (cat != null && editData.getValue() == null) editData.setValue(cat);
        });
    }

    public LiveData<CustomItemCategoryEntity> getEditData() { return editData; }
    public LiveData<Boolean> getSaveResult() { return saveResult; }

    public void save(CustomItemCategoryEntity cat) {
        executor.execute(() -> {
            try {
                if (isEditMode()) {
                    cat.id = editId;
                    dao.update(cat);
                } else {
                    if (dao.countByName(cat.name) > 0) {
                        saveResult.postValue(false);
                        return;
                    }
                    dao.insert(cat);
                }
                saveResult.postValue(true);
            } catch (Exception e) {
                saveResult.postValue(false);
            }
        });
    }

    public static class Factory extends ViewModelProvider.NewInstanceFactory {
        private final CustomItemCategoryDao dao;
        private final long editId;
        private final Executor executor;
        public Factory(CustomItemCategoryDao dao, long editId, Executor executor) {
            this.dao = dao; this.editId = editId; this.executor = executor;
        }
        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            return (T) new CustomItemCategoryCreateViewModel(dao, editId, executor);
        }
    }
}