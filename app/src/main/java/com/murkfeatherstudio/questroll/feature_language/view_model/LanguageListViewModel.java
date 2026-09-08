package com.murkfeatherstudio.questroll.feature_language.view_model;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.murkfeatherstudio.questroll.core.models.custom.custom_language.CustomLanguageEntity;
import com.murkfeatherstudio.questroll.core.models.open5e.language.LanguageEntity;
import com.murkfeatherstudio.questroll.feature_language.data.repository.LanguageRepository;
import com.murkfeatherstudio.questroll.feature_language.model.CombinedLanguage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class LanguageListViewModel extends ViewModel {
    private final LanguageRepository repository;
    private final MediatorLiveData<List<CombinedLanguage>> combinedLanguages = new MediatorLiveData<>();
    
    private final MutableLiveData<String> query = new MutableLiveData<>("");
    private final MutableLiveData<String> typeFilter = new MutableLiveData<>("All"); // All, Official, Custom
    
    private List<LanguageEntity> lastOpen5e = Collections.emptyList();
    private List<CustomLanguageEntity> lastCustom = Collections.emptyList();

    public LanguageListViewModel(LanguageRepository repository) {
        this.repository = repository;
        setupDataSources();
    }

    private void setupDataSources() {
        LiveData<List<LanguageEntity>> open5eLive = repository.getAllOpen5eLanguages();
        LiveData<List<CustomLanguageEntity>> customLive = repository.getAllCustomLanguages();

        combinedLanguages.addSource(customLive, customList -> {
            lastCustom = customList != null ? customList : Collections.emptyList();
            combineData();
        });

        combinedLanguages.addSource(open5eLive, open5eList -> {
            lastOpen5e = open5eList != null ? open5eList : Collections.emptyList();
            combineData();
        });
    }

    private void combineData() {
        repository.getExecutor().execute(() -> {
            List<CombinedLanguage> combined = new ArrayList<>();
            for (LanguageEntity le : lastOpen5e) {
                combined.add(mapOpen5eEntity(le, resolveScriptName(le.scriptLanguage)));
            }
            for (CustomLanguageEntity ce : lastCustom) {
                combined.add(mapCustomEntity(ce, resolveScriptName(ce.scriptLanguageId)));
            }
            combinedLanguages.postValue(combined);
        });
    }

    public LiveData<List<CombinedLanguage>> getFilteredLanguages() {
        return Transformations.switchMap(combinedLanguages, list -> 
            Transformations.switchMap(query, q -> 
                Transformations.switchMap(typeFilter, type -> {
                    MutableLiveData<List<CombinedLanguage>> liveData = new MutableLiveData<>();
                    List<CombinedLanguage> filtered = list.stream()
                        .filter(l -> q.isEmpty() || l.name.toLowerCase().contains(q.toLowerCase()))
                        .filter(l -> {
                            if (type.equals("Custom")) return l.isCustom();
                            if (type.equals("Official")) return !l.isCustom();
                            return true;
                        })
                        .collect(Collectors.toList());
                    liveData.setValue(filtered);
                    return liveData;
                })
            )
        );
    }

    public void setQuery(String q) { query.setValue(q); }
    public void setTypeFilter(String type) { typeFilter.setValue(type); }

    private String resolveScriptName(String scriptId) {
        if (scriptId == null || scriptId.isEmpty()) return null;
        try {
            if (scriptId.contains("/")) {
                String[] parts = scriptId.split("/");
                LanguageEntity open5e = repository.getOpen5eDao().getByKey(parts[parts.length - 1]);
                return open5e != null ? open5e.name : "Unknown Script";
            }
            LanguageEntity open5e = repository.getOpen5eDao().getByKey(scriptId);
            if (open5e != null) return open5e.name;
            try {
                long customId = Long.parseLong(scriptId);
                CustomLanguageEntity custom = repository.getCustomDao().findById(customId);
                return custom != null ? custom.name : "Unknown Script";
            } catch (NumberFormatException e) { return "Invalid ID"; }
        } catch (Exception e) { return "Error"; }
    }

    private CombinedLanguage mapOpen5eEntity(LanguageEntity entity, String scriptName) {
        return new CombinedLanguage(entity.name, entity.desc, entity.isExotic, entity.isSecret, scriptName, entity.scriptLanguage, null, entity.document, entity.key, null);
    }

    private CombinedLanguage mapCustomEntity(CustomLanguageEntity entity, String scriptName) {
        return new CombinedLanguage(entity.name, entity.desc, entity.isExotic, entity.isSecret, scriptName, null, entity.scriptLanguageId, null, null, entity.id);
    }
}
