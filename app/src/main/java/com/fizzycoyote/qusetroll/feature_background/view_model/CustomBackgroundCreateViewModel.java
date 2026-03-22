package com.fizzycoyote.qusetroll.feature_background.view_model;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.fizzycoyote.qusetroll.core.models.custom.custom_background.CustomBackgroundDao;
import com.fizzycoyote.qusetroll.core.models.custom.custom_background.CustomBackgroundEntity;
import com.fizzycoyote.qusetroll.core.models.open5e.background.BackgroundDto;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

public class CustomBackgroundCreateViewModel extends ViewModel {

    public static final long NO_ID = -1L;

    private final CustomBackgroundDao dao;
    private final long editId;
    private final Executor executor;

    private final MutableLiveData<CustomBackgroundEntity> editData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> saveResult = new MutableLiveData<>();
    private final MutableLiveData<List<BackgroundDto.BenefitDto>> benefits =
            new MutableLiveData<>(new ArrayList<>());

    public CustomBackgroundCreateViewModel(CustomBackgroundDao dao, long editId, Executor executor) {
        this.dao = dao;
        this.editId = editId;
        this.executor = executor;
        if (isEditMode()) loadExisting();
    }

    public boolean isEditMode() { return editId != NO_ID; }

    private void loadExisting() {
        dao.getById(editId).observeForever(b -> {
            if (b != null && editData.getValue() == null) {
                editData.setValue(b);
                try {
                    Type type = new TypeToken<List<BackgroundDto.BenefitDto>>(){}.getType();
                    List<BackgroundDto.BenefitDto> list = new Gson().fromJson(b.benefitsJson, type);
                    benefits.setValue(list != null ? list : new ArrayList<>());
                } catch (Exception ignored) {}
            }
        });
    }

    public LiveData<CustomBackgroundEntity> getEditData() { return editData; }
    public LiveData<Boolean> getSaveResult() { return saveResult; }
    public LiveData<List<BackgroundDto.BenefitDto>> getBenefits() { return benefits; }

    public void addBenefit(String name, String desc, String type) {
        BackgroundDto.BenefitDto b = new BackgroundDto.BenefitDto();
        b.name = name; b.desc = desc; b.type = type;
        List<BackgroundDto.BenefitDto> list = new ArrayList<>(
                benefits.getValue() != null ? benefits.getValue() : new ArrayList<>());
        list.add(b); benefits.setValue(list);
    }

    public void removeBenefit(int i) {
        List<BackgroundDto.BenefitDto> list = new ArrayList<>(
                benefits.getValue() != null ? benefits.getValue() : new ArrayList<>());
        if (i >= 0 && i < list.size()) { list.remove(i); benefits.setValue(list); }
    }

    public void save(String name, String desc) {
        executor.execute(() -> {
            try {
                CustomBackgroundEntity e = new CustomBackgroundEntity();
                if (isEditMode()) e.id = editId;
                e.name = name;
                e.desc = desc;
                e.benefitsJson = new Gson().toJson(benefits.getValue());

                if (isEditMode()) { dao.update(e); }
                else {
                    if (dao.countByName(name) > 0) { saveResult.postValue(false); return; }
                    dao.insert(e);
                }
                saveResult.postValue(true);
            } catch (Exception ex) { saveResult.postValue(false); }
        });
    }

    public static class Factory extends ViewModelProvider.NewInstanceFactory {
        private final CustomBackgroundDao dao;
        private final long editId;
        private final Executor executor;

        public Factory(CustomBackgroundDao dao, long editId, Executor executor) {
            this.dao = dao; this.editId = editId; this.executor = executor;
        }

        @NonNull @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            return (T) new CustomBackgroundCreateViewModel(dao, editId, executor);
        }
    }
}
