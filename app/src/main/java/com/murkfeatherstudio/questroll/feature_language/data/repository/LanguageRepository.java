package com.murkfeatherstudio.questroll.feature_language.data.repository;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.core.util.Consumer;
import androidx.lifecycle.LiveData;

import com.murkfeatherstudio.questroll.core.models.custom.custom_language.CustomLanguageDao;
import com.murkfeatherstudio.questroll.core.models.custom.custom_language.CustomLanguageEntity;
import com.murkfeatherstudio.questroll.core.models.open5e.document.DocumentDao;
import com.murkfeatherstudio.questroll.core.models.open5e.document.DocumentEntity;
import com.murkfeatherstudio.questroll.core.models.open5e.language.LanguageDao;
import com.murkfeatherstudio.questroll.core.models.open5e.language.LanguageEntity;
import com.murkfeatherstudio.questroll.feature_language.model.CombinedLanguage;

import java.util.List;
import java.util.concurrent.Executor;

/**
 * Repository for language data, providing a unified interface for both official
 * Open5e languages and user-created custom languages.
 * <p>
 * It handles script resolution, document lookup for licensing information, and
 * CRUD operations for custom languages.
 * </p>
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

    /**
     * Retrieves a merged language object from either source.
     * Open5e data is prioritized if a key is provided.
     *
     * @param open5eKey Unique key for an official language.
     * @param customId  Unique ID for a custom language.
     * @param onSuccess Callback for the resulting {@link CombinedLanguage}.
     * @param onError   Callback for error handling.
     */
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

    /**
     * Deletes a custom language by its ID.
     */
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

    /**
     * Looks up a source document by its URL.
     */
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

    /**
     * Looks up a source document by its unique key.
     */
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

    /**
     * Retrieves the language used for a script (e.g. searching "elvish" when looking
     * for the script for another language).
     */
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
                entity.scriptLanguage,
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
     * Resolves the name of a script from its URL or unique key.
     * <p>
     * Searches both official and custom language lists to find the display name.
     * </p>
     *
     * @param scriptUrlOrKey The identifier for the script language.
     * @return The display name of the script language, or "Unknown".
     */
    public String resolveScriptName(String scriptUrlOrKey) {
        if (scriptUrlOrKey == null) return null;

        String key = scriptUrlOrKey;
        if (scriptUrlOrKey.startsWith("http")) {
            key = extractKeyFromUrl(scriptUrlOrKey);
        }

        try {
            LanguageEntity open5e = open5eDao.getByKey(key);
            if (open5e != null) return open5e.name;

            long customId = Long.parseLong(key);
            CustomLanguageEntity custom = customDao.findById(customId);
            return custom != null ? custom.name : "Unknown";
        } catch (NumberFormatException e) {
            return "Unknown";
        } catch (Exception e) {
            return "Invalid";
        }
    }

    private String extractKeyFromUrl(String url) {
        if (url == null) return null;
        String u = url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
        String[] parts = u.split("/");
        return parts.length > 0 ? parts[parts.length - 1] : null;
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
