package com.murkfeatherstudio.questroll.feature_item.item_rarity.view_model;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.murkfeatherstudio.questroll.core.models.custom.custom_item_rarity.CustomItemRarityDao;
import com.murkfeatherstudio.questroll.core.models.custom.custom_item_rarity.CustomItemRarityEntity;

import java.util.concurrent.Executor;

public class CustomItemRarityCreateViewModel extends ViewModel {
    private final CustomItemRarityDao dao;
    private final long editId;
    private final Executor executor;

    private final MutableLiveData<CustomItemRarityEntity> editData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> saveResult = new MutableLiveData<>();

    public CustomItemRarityCreateViewModel(CustomItemRarityDao dao, long editId, Executor executor) {
        this.dao = dao;
        this.editId = editId;
        this.executor = executor;
        if (isEditMode()) loadExisting();
    }

    public boolean isEditMode() { return editId != -1; }

    private void loadExisting() {
        dao.getById(editId).observeForever(rarity -> {
            if (rarity != null && editData.getValue() == null) {
                editData.setValue(rarity);
            }
        });
    }

    public LiveData<CustomItemRarityEntity> getEditData() { return editData; }
    public LiveData<Boolean> getSaveResult() { return saveResult; }

    public void save(CustomItemRarityEntity rarity) {
        executor.execute(() -> {
            try {
                if (isEditMode()) {
                    rarity.id = editId;
                    dao.update(rarity);
                } else {
                    if (dao.countByName(rarity.name) > 0) {
                        saveResult.postValue(false);
                        return;
                    }
                    dao.insert(rarity);
                }
                saveResult.postValue(true);
            } catch (Exception e) {
                saveResult.postValue(false);
            }
        });
    }

    public static class Factory extends ViewModelProvider.NewInstanceFactory {
        private final CustomItemRarityDao dao;
        private final long editId;
        private final Executor executor;

        public Factory(CustomItemRarityDao dao, long editId, Executor executor) {
            this.dao = dao; this.editId = editId; this.executor = executor;
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            return (T) new CustomItemRarityCreateViewModel(dao, editId, executor);
        }
    }
}