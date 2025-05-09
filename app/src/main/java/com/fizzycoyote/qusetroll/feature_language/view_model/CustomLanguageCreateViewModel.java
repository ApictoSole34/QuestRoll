package com.fizzycoyote.qusetroll.feature_language.view_model;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.fizzycoyote.qusetroll.core.models.custom.custom_language.CustomLanguageEntity;
import com.fizzycoyote.qusetroll.core.models.open5e.language.LanguageEntity;
import com.fizzycoyote.qusetroll.feature_language.data.repository.LanguageRepository;
import com.fizzycoyote.qusetroll.feature_language.model.item.ScriptItem;

import java.util.ArrayList;
import java.util.List;

public class CustomLanguageCreateViewModel extends ViewModel {
    private final LanguageRepository repository;
    private final MutableLiveData<List<ScriptItem>> scriptOptions = new MutableLiveData<>();
    private final MutableLiveData<CustomLanguageEntity> existingLanguage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> saveResult = new MutableLiveData<>();

    public CustomLanguageCreateViewModel(@NonNull LanguageRepository repository) {
        this.repository = repository;
    }

    public void loadScriptOptions(long excludeId) {
        repository.getExecutor().execute(() -> {
            List<ScriptItem> items = new ArrayList<>();
            items.add(new ScriptItem("None", null));

            // Open5E
            List<LanguageEntity> open5e = repository.getOpen5eDao().getAllLanguages();
            for (LanguageEntity le : open5e) {
                items.add(new ScriptItem(le.name, le.key));
            }

            // Custom
            List<CustomLanguageEntity> customs = repository.getCustomDao().getAll();
            for (CustomLanguageEntity ce : customs) {
                if (ce.id != excludeId) {
                    items.add(new ScriptItem(ce.name, String.valueOf(ce.id)));
                }
            }

            scriptOptions.postValue(items);
        });
    }

    public void loadExistingLanguage(long id) {
        repository.getExecutor().execute(() -> {
            CustomLanguageEntity entity = repository.getCustomDao().findById(id);
            existingLanguage.postValue(entity);
        });
    }

    public void saveLanguage(String name, String desc, boolean exotic, boolean secret,
                             String scriptId, long existingId) {
        repository.getExecutor().execute(() -> {
            try {
                CustomLanguageEntity entity = new CustomLanguageEntity(name, desc);
                entity.isExotic = exotic;
                entity.isSecret = secret;
                entity.scriptLanguageId = scriptId;

                if (existingId != -1) {
                    entity.id = existingId;
                    repository.getCustomDao().update(entity);
                } else {
                    if (repository.getCustomDao().countByName(name) > 0) {
                        saveResult.postValue(false);
                        return;
                    }
                    repository.getCustomDao().insert(entity);
                }

                saveResult.postValue(true);
            } catch (Exception e) {
                saveResult.postValue(false);
            }
        });
    }

    public LiveData<List<ScriptItem>> getScriptOptions() { return scriptOptions; }
    public LiveData<CustomLanguageEntity> getExistingLanguage() { return existingLanguage; }
    public LiveData<Boolean> getSaveResult() { return saveResult; }
}