package com.fizzycoyote.qusetroll.feature_class.view_model;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.fizzycoyote.qusetroll.feature_class.repository.ClassRepository;

public class ClassListViewModelFactory implements ViewModelProvider.Factory {
    private final ClassRepository repository;

    public ClassListViewModelFactory(ClassRepository repository) {
        this.repository = repository;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new ClassListViewModel(repository);
    }
}