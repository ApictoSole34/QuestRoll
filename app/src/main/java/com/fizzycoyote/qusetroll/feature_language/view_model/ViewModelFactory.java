package com.fizzycoyote.qusetroll.feature_language.view_model;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.fizzycoyote.qusetroll.feature_language.data.repository.LanguageRepository;

public class ViewModelFactory implements ViewModelProvider.Factory {
    private final LanguageRepository repository;

    public ViewModelFactory(LanguageRepository repository) {
        this.repository = repository;
    }

    @SuppressWarnings("unchecked")
    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(LanguageDetailViewModel.class)) {
            return (T) new LanguageDetailViewModel(repository);
        }
        if (modelClass.isAssignableFrom(LanguageListViewModel.class)) {
            return (T) new LanguageListViewModel(repository);
        }
        if (modelClass.isAssignableFrom(CustomLanguageCreateViewModel.class)) {
            return (T) new CustomLanguageCreateViewModel(repository);
        }
        throw new IllegalArgumentException("Unknown ViewModel class: " + modelClass.getName());
    }
}