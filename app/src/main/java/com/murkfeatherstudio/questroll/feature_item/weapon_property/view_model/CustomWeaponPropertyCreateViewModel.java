package com.murkfeatherstudio.questroll.feature_item.weapon_property.view_model;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.murkfeatherstudio.questroll.core.models.custom.custom_weapon_property.CustomWeaponPropertyDao;
import com.murkfeatherstudio.questroll.core.models.custom.custom_weapon_property.CustomWeaponPropertyEntity;

import java.util.concurrent.Executor;

public class CustomWeaponPropertyCreateViewModel extends ViewModel {
    private final CustomWeaponPropertyDao dao;
    private final long editId;
    private final Executor executor;

    private final MutableLiveData<CustomWeaponPropertyEntity> editData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> saveResult = new MutableLiveData<>();

    public CustomWeaponPropertyCreateViewModel(CustomWeaponPropertyDao dao, long editId, Executor executor) {
        this.dao = dao;
        this.editId = editId;
        this.executor = executor;
        if (isEditMode()) loadExisting();
    }

    public boolean isEditMode() { return editId != -1; }

    private void loadExisting() {
        dao.getById(editId).observeForever(prop -> {
            if (prop != null && editData.getValue() == null) {
                editData.setValue(prop);
            }
        });
    }

    public LiveData<CustomWeaponPropertyEntity> getEditData() { return editData; }
    public LiveData<Boolean> getSaveResult() { return saveResult; }

    public void save(CustomWeaponPropertyEntity prop) {
        executor.execute(() -> {
            try {
                if (isEditMode()) {
                    prop.id = editId;
                    dao.update(prop);
                } else {
                    if (dao.countByName(prop.name) > 0) {
                        saveResult.postValue(false);
                        return;
                    }
                    dao.insert(prop);
                }
                saveResult.postValue(true);
            } catch (Exception e) {
                saveResult.postValue(false);
            }
        });
    }

    public static class Factory extends ViewModelProvider.NewInstanceFactory {
        private final CustomWeaponPropertyDao dao;
        private final long editId;
        private final Executor executor;
        public Factory(CustomWeaponPropertyDao dao, long editId, Executor executor) {
            this.dao = dao; this.editId = editId; this.executor = executor;
        }
        @NonNull @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            return (T) new CustomWeaponPropertyCreateViewModel(dao, editId, executor);
        }
    }
}
