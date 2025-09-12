package com.fizzycoyote.qusetroll.feature_language.data.repository;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.core.util.Consumer;
import androidx.lifecycle.LiveData;

import com.fizzycoyote.qusetroll.core.models.custom.custom_language.CustomLanguageDao;
import com.fizzycoyote.qusetroll.core.models.custom.custom_language.CustomLanguageEntity;
import com.fizzycoyote.qusetroll.core.models.open5e.document.DocumentDao;
import com.fizzycoyote.qusetroll.core.models.open5e.document.DocumentEntity;
import com.fizzycoyote.qusetroll.core.models.open5e.language.LanguageDao;
import com.fizzycoyote.qusetroll.core.models.open5e.language.LanguageEntity;
import com.fizzycoyote.qusetroll.feature_language.model.CombinedLanguage;

import java.util.List;
import java.util.concurrent.Executor;

/**
 *Central data access point for languages, combining Open5e API data and user-created content.
 */
public class LanguageRepository {

    private final DocumentDao open5eDocumentDao;
    private final LanguageDao open5eDao;        // External Open5e API languages
    private final CustomLanguageDao customDao;  // User-created custom languages
    private final Executor executor;            // Background task executor

    public LanguageRepository(DocumentDao open5eDocumentDao, LanguageDao open5eDao,
                              CustomLanguageDao customDao,
                              Executor executor) {
        this.open5eDocumentDao = open5eDocumentDao;
        this.open5eDao = open5eDao;
        this.customDao = customDao;
        this.executor = executor;
    }

    public void getCombinedLanguage(String open5eKey, Long customId,
                                    Consumer<CombinedLanguage> onSuccess,
                                    Consumer<Exception> onError) {
        executor.execute(() -> {
            try {
                // Priority: Open5e key takes precedence over custom ID
                if (open5eKey != null) {
                    LanguageEntity entity = open5eDao.getByKey(open5eKey);
                    onSuccess.accept(mapOpen5eEntity(entity));
                } else if (customId != null) {
                    CustomLanguageEntity entity = customDao.findById(customId);
                    onSuccess.accept(mapCustomEntity(entity));
                }
            } catch (Exception e) {
                onError.accept(e);
            }
        });
    }

    public void deleteLanguage(long id, Runnable onSuccess, Consumer<Exception> onError) {
        executor.execute(() -> {
            try {
                customDao.deleteById(id);
                runOnUiThread(onSuccess);
            } catch (Exception e) {
                runOnUiThread(() -> onError.accept(e));
            }
        });
    }

    private void runOnUiThread(Runnable action) {
        new Handler(Looper.getMainLooper()).post(action);
    }

    public void getDocumentByUrl(String url,
                                 Consumer<DocumentEntity> onSuccess,
                                 Consumer<Exception> onError) {
        executor.execute(() -> {
            try {
                DocumentEntity doc = open5eDocumentDao.getByUrl(url);
                onSuccess.accept(doc);
            } catch (Exception e) {
                onError.accept(e);
            }
        });
    }

    public void getDocumentByKey(String key,
                                 Consumer<DocumentEntity> onSuccess,
                                 Consumer<Exception> onError) {
        executor.execute(() -> {
            try {
                DocumentEntity doc = open5eDocumentDao.getByKey(key);

                if (doc != null) {
                    onSuccess.accept(doc);
                } else {
                    onError.accept(new Exception("Document not found for key: " + key));
                }
            } catch (Exception e) {
                Log.e("License", "Error querying document", e);
                onError.accept(e);
            }
        });
    }

    public void getScriptLanguage(String key,
                                  Consumer<CombinedLanguage> onSuccess,
                                  Consumer<Exception> onError) {
        executor.execute(() -> {
            try {
                if (isNumeric(key)) {
                    long customId = Long.parseLong(key);
                    CustomLanguageEntity entity = customDao.findById(customId);
                    onSuccess.accept(mapCustomEntity(entity));
                } else {
                    LanguageEntity entity = open5eDao.getByKey(key);
                    onSuccess.accept(mapOpen5eEntity(entity));
                }
            } catch (Exception e) {
                onError.accept(e);
            }
        });
    }

    private boolean isNumeric(String str) {
        return str.matches("-?\\d+(\\.\\d+)?");
    }


private CombinedLanguage mapOpen5eEntity(LanguageEntity entity) {
        return new CombinedLanguage(
                entity.name,
                entity.desc,
                entity.isExotic,
                entity.isSecret,
                resolveScriptName(entity.scriptLanguage),
                entity.url,
                null,
                entity.document,
                entity.key,
                null
        );
    }

    private CombinedLanguage mapCustomEntity(CustomLanguageEntity entity) {
        return new CombinedLanguage(
                entity.name,
                entity.desc,
                entity.isExotic,
                entity.isSecret,
                resolveScriptName(entity.scriptLanguageId),
                null,
                entity.scriptLanguageId,
                null,
                null,
                entity.id
        );
    }

    /**
     *Resolves script name from either Open5e or custom sources
     *Example: "elvish" → Open5e name, "42" → custom language name
     *Returns "Unknown" if not found
     */
    public String resolveScriptName(String scriptId) {
        if (scriptId == null) return null;
        try {
            LanguageEntity open5e = open5eDao.getByKey(scriptId);
            if (open5e != null) return open5e.name;

            long customId = Long.parseLong(scriptId);
            CustomLanguageEntity custom = customDao.findById(customId);
            return custom != null ? custom.name : "Unknown";
        } catch (Exception e) {
            return "Invalid";
        }
    }

    public LiveData<List<LanguageEntity>> getAllOpen5eLanguages() {
        return open5eDao.getAllLive();
    }

    public LiveData<List<CustomLanguageEntity>> getAllCustomLanguages() {
        return customDao.getAllLive();
    }

    public LanguageDao getOpen5eDao() {
        return open5eDao;
    }

    public CustomLanguageDao getCustomDao() {
        return customDao;
    }

    public Executor getExecutor() {
        return executor;
    }
}