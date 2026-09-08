package com.murkfeatherstudio.questroll.feature_spell.spell_school.view_model;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import com.murkfeatherstudio.questroll.core.models.custom.custom_spell.CustomSpellSchoolDao;
import com.murkfeatherstudio.questroll.core.models.custom.custom_spell.CustomSpellSchoolEntity;
import java.util.concurrent.Executor;

public class CustomSpellSchoolCreateViewModel extends ViewModel {
    private final CustomSpellSchoolDao dao;
    private final long editId;
    private final Executor executor;
    private final MutableLiveData<CustomSpellSchoolEntity> editData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> saveResult = new MutableLiveData<>();

    public CustomSpellSchoolCreateViewModel(CustomSpellSchoolDao dao, long editId, Executor executor) {
        this.dao = dao;
        this.editId = editId;
        this.executor = executor;
        if (isEditMode()) loadExisting();
    }

    public boolean isEditMode() { return editId != -1; }

    private void loadExisting() {
        dao.getById(editId).observeForever(school -> {
            if (school != null && editData.getValue() == null) editData.setValue(school);
        });
    }

    public LiveData<CustomSpellSchoolEntity> getEditData() { return editData; }
    public LiveData<Boolean> getSaveResult() { return saveResult; }

    public void save(CustomSpellSchoolEntity school) {
        executor.execute(() -> {
            try {
                if (isEditMode()) {
                    school.id = editId;
                    dao.update(school);
                } else {
                    if (dao.countByName(school.name) > 0) {
                        saveResult.postValue(false);
                        return;
                    }
                    dao.insert(school);
                }
                saveResult.postValue(true);
            } catch (Exception e) {
                saveResult.postValue(false);
            }
        });
    }

    public static class Factory extends ViewModelProvider.NewInstanceFactory {
        private final CustomSpellSchoolDao dao;
        private final long editId;
        private final Executor executor;
        public Factory(CustomSpellSchoolDao dao, long editId, Executor executor) {
            this.dao = dao; this.editId = editId; this.executor = executor;
        }
        @NonNull @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            return (T) new CustomSpellSchoolCreateViewModel(dao, editId, executor);
        }
    }
}