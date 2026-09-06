package com.fizzycoyote.qusetroll.feature_language.view_model;

import android.util.Log;

import androidx.core.util.Consumer;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.fizzycoyote.qusetroll.core.models.open5e.document.DocumentEntity;
import com.fizzycoyote.qusetroll.feature_language.data.repository.LanguageRepository;
import com.fizzycoyote.qusetroll.feature_language.model.CombinedLanguage;

/**
 * ViewModel for the Language Detail screen, managing the display, deletion, and
 * document lookup for a specific language.
 * <p>
 * It interacts with the {@link LanguageRepository} to fetch data from both
 * official and custom sources.
 * </p>
 */
public class LanguageDetailViewModel extends ViewModel {
    private final LanguageRepository repository;
    private final MutableLiveData<CombinedLanguage> language = new MutableLiveData<>();
    private final MutableLiveData<DocumentEntity> document = new MutableLiveData<>();
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private final MutableLiveData<Boolean> deleteSuccess = new MutableLiveData<>();

    public LanguageDetailViewModel(LanguageRepository repository) {
        this.repository = repository;
    }

    /**
     * Loads language details from the repository using either an official key or a custom ID.
     *
     * @param open5eKey Unique key for an official language.
     * @param customId  Unique ID for a custom language.
     */
    public void loadLanguage(String open5eKey, Long customId) {
        repository.getCombinedLanguage(open5eKey, customId,
                language::postValue,
                e -> error.postValue("Error: " + e.getMessage())
        );
    }

    /**
     * Loads the source document details for a language to display license information.
     *
     * @param key The document key.
     */
    public void loadDocument(String key) {
        if (key == null) {
            error.postValue("Document key is null");
            return;
        }

        repository.getDocumentByKey(key,
                doc -> document.postValue(doc),
                e -> {
                    Log.e("License", "Error loading document", e);
                    error.postValue("License error: " + e.getMessage());
                }
        );
    }

    /**
     * Deletes a custom language from the database.
     *
     * @param customId The unique ID of the custom language.
     */
    public void deleteLanguage(long customId) {
        repository.deleteLanguage(customId,
                () -> {
                    deleteSuccess.postValue(true);
                    language.postValue(null);
                },
                e -> error.postValue("Delete error: " + e.getMessage())
        );
    }

    /**
     * Helper method to lookup a language used as a script.
     */
    public void getScriptLanguage(String key,
                                  Consumer<CombinedLanguage> onSuccess,
                                  Consumer<Exception> onError) {
        repository.getScriptLanguage(key, onSuccess, onError);
    }

    public LiveData<Boolean> getDeleteSuccess() {return deleteSuccess;}
    public LiveData<CombinedLanguage> getLanguage() { return language; }
    public LiveData<DocumentEntity> getDocument() { return document; }
    public LiveData<String> getError() { return error; }
}
