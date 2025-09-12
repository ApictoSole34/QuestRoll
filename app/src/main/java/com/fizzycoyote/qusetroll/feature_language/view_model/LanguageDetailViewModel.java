package com.fizzycoyote.qusetroll.feature_language.view_model;

import android.util.Log;

import androidx.core.util.Consumer;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.fizzycoyote.qusetroll.core.models.open5e.document.DocumentEntity;
import com.fizzycoyote.qusetroll.feature_language.data.repository.LanguageRepository;
import com.fizzycoyote.qusetroll.feature_language.model.CombinedLanguage;

public class LanguageDetailViewModel extends ViewModel {
    private final LanguageRepository repository;
    private final MutableLiveData<CombinedLanguage> language = new MutableLiveData<>();
    private final MutableLiveData<DocumentEntity> document = new MutableLiveData<>();
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private final MutableLiveData<Boolean> deleteSuccess = new MutableLiveData<>();

    public LanguageDetailViewModel(LanguageRepository repository) {
        this.repository = repository;
    }

    public void loadLanguage(String open5eKey, Long customId) {
        repository.getCombinedLanguage(open5eKey, customId,
                language::postValue,
                e -> error.postValue("Error: " + e.getMessage())
        );
    }

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

    public void deleteLanguage(long customId) {
        repository.deleteLanguage(customId,
                () -> {
                    deleteSuccess.postValue(true);
                    language.postValue(null);
                },
                e -> error.postValue("Delete error: " + e.getMessage())
        );
    }

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