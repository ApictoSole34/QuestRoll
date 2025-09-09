package com.fizzycoyote.qusetroll.feature_class.view_model;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.fizzycoyote.qusetroll.feature_class.model.CombinedClass;
import com.fizzycoyote.qusetroll.feature_class.repository.ClassRepository;

import java.util.List;

public class ClassListViewModel extends ViewModel {
    private final LiveData<List<CombinedClass>> combinedClasses;

    public ClassListViewModel(ClassRepository repository) {
        combinedClasses = repository.getCombinedClasses();
    }

    public LiveData<List<CombinedClass>> getCombinedClasses() {
        return combinedClasses;
    }

    public static class Factory implements ViewModelProvider.Factory {
        private final ClassRepository repository;

        public Factory(ClassRepository repository) {
            this.repository = repository;
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            return (T) new ClassListViewModel(repository);
        }
    }
}