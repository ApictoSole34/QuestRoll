package com.fizzycoyote.qusetroll.feature_item.item_set.view_model;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import com.fizzycoyote.qusetroll.core.models.custom.custom_item_set.CustomItemSetDao;
import com.fizzycoyote.qusetroll.core.models.custom.custom_item_set.CustomItemSetEntity;
import java.util.concurrent.Executor;

public class CustomItemSetCreateViewModel extends ViewModel {
    private final CustomItemSetDao dao;
    private final long editId;
    private final Executor executor;
    private final MutableLiveData<CustomItemSetEntity> editData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> saveResult = new MutableLiveData<>();

    public CustomItemSetCreateViewModel(CustomItemSetDao dao, long editId, Executor executor) {
        this.dao = dao; this.editId = editId; this.executor = executor;
        if (isEditMode()) loadExisting();
    }
    public boolean isEditMode() { return editId != -1; }
    private void loadExisting() {
        dao.getById(editId).observeForever(set -> {
            if (set != null && editData.getValue() == null) editData.setValue(set);
        });
    }
    public LiveData<CustomItemSetEntity> getEditData() { return editData; }
    public LiveData<Boolean> getSaveResult() { return saveResult; }
    public void save(CustomItemSetEntity set) {
        executor.execute(() -> {
            try {
                if (isEditMode()) { set.id = editId; dao.update(set); }
                else { if (dao.countByName(set.name) > 0) { saveResult.postValue(false); return; } dao.insert(set); }
                saveResult.postValue(true);
            } catch (Exception e) { saveResult.postValue(false); }
        });
    }
    public static class Factory extends ViewModelProvider.NewInstanceFactory {
        private final CustomItemSetDao dao; private final long editId; private final Executor executor;
        public Factory(CustomItemSetDao dao, long editId, Executor executor) { this.dao = dao; this.editId = editId; this.executor = executor; }
        @NonNull @Override public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            return (T) new CustomItemSetCreateViewModel(dao, editId, executor);
        }
    }
}
