package com.fizzycoyote.qusetroll.feature_class.view_model;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.fizzycoyote.qusetroll.feature_class.repository.ClassRepository;

public class ClassCreateViewModelFactory implements ViewModelProvider.Factory {

    private final ClassRepository repository;
    private final long editClassId;

    public ClassCreateViewModelFactory(ClassRepository repository) {
        this.repository = repository;
        this.editClassId = ClassCreateViewModel.NO_ID;
    }

    public ClassCreateViewModelFactory(ClassRepository repository, long editClassId) {
        this.repository = repository;
        this.editClassId = editClassId;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new ClassCreateViewModel(repository, editClassId);
    }
}
