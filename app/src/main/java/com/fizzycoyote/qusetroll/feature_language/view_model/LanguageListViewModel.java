package com.fizzycoyote.qusetroll.feature_language.view_model;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.ViewModel;

import com.fizzycoyote.qusetroll.core.models.custom.custom_language.CustomLanguageEntity;
import com.fizzycoyote.qusetroll.core.models.open5e.language.LanguageEntity;
import com.fizzycoyote.qusetroll.feature_language.data.repository.LanguageRepository;
import com.fizzycoyote.qusetroll.feature_language.model.CombinedLanguage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * ViewModel for the Language List screen, providing a merged view of languages
 * from both the official Open5e compendium and user-created custom content.
 * <p>
 * It handles script resolution for both data sources to ensure consistent display
 * in the UI.
 * </p>
 */
public class LanguageListViewModel extends ViewModel {
    private final LanguageRepository repository;
    private final MediatorLiveData<List<CombinedLanguage>> combinedLanguages = new MediatorLiveData<>();

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
            if (customList != null) {
                lastCustom = customList;
                combineData();
            }
        });

        combinedLanguages.addSource(open5eLive, open5eList -> {
            if (open5eList != null) {
                lastOpen5e = open5eList;
                combineData();
            }
        });
    }

    /**
     * Merges official and custom languages into a single list and resolves their script names.
     */
    private void combineData() {
        repository.getExecutor().execute(() -> {
            List<CombinedLanguage> combined = new ArrayList<>();

            for (LanguageEntity le : lastOpen5e) {
                String scriptName = resolveScriptName(le.scriptLanguage);
                combined.add(mapOpen5eEntity(le, scriptName));
            }

            for (CustomLanguageEntity ce : lastCustom) {
                String scriptName = resolveScriptName(ce.scriptLanguageId);
                combined.add(mapCustomEntity(ce, scriptName));
            }

            combinedLanguages.postValue(combined);
        });
    }

    /**
     * Resolves the display name of a script from its ID or URL by checking
     * both official and custom sources.
     */
    private String resolveScriptName(String scriptId) {
        if (scriptId == null || scriptId.isEmpty()) {
            return null;
        }

        try {
            if (scriptId.contains("/")) {
                String[] parts = scriptId.split("/");
                String open5eKey = parts[parts.length - 1];
                LanguageEntity open5e = repository.getOpen5eDao().getByKey(open5eKey);
                return open5e != null ? open5e.name : "Unknown Open5E Script";
            }

            LanguageEntity open5e = repository.getOpen5eDao().getByKey(scriptId);
            if (open5e != null) {
                return open5e.name;
            }

            try {
                long customId = Long.parseLong(scriptId);
                CustomLanguageEntity custom = repository.getCustomDao().findById(customId);
                return custom != null ? custom.name : "Unknown Custom Script";
            } catch (NumberFormatException e) {
                return "Invalid Script ID";
            }

        } catch (Exception e) {
            return "Error Resolving Script";
        }
    }

    private CombinedLanguage mapOpen5eEntity(LanguageEntity entity, String scriptName) {

        return new CombinedLanguage(
                entity.name,
                entity.desc,
                entity.isExotic,
                entity.isSecret,
                scriptName,
                entity.scriptLanguage,  // scriptOpenUrl
                null,                   // scriptCustomKey
                entity.document,
                entity.key,
                null
        );
    }

    private CombinedLanguage mapCustomEntity(CustomLanguageEntity entity, String scriptName) {
        String scriptCustomKey = null;
        String scriptOpenUrl = null;

        if (entity.scriptLanguageId != null) {
            if (entity.scriptLanguageId.matches("\\d+")) {
                scriptCustomKey = entity.scriptLanguageId;
            } else {
                scriptOpenUrl = entity.scriptLanguageId;
            }
        }

        return new CombinedLanguage(
                entity.name,
                entity.desc,
                entity.isExotic,
                entity.isSecret,
                scriptName,
                scriptOpenUrl,
                scriptCustomKey,
                null,
                null,
                entity.id
        );
    }

    public LiveData<List<CombinedLanguage>> getCombinedLanguages() {
        return combinedLanguages;
    }
}
