package com.murkfeatherstudio.questroll.feature_class.view_model;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.murkfeatherstudio.questroll.feature_class.model.CombinedClass;
import com.murkfeatherstudio.questroll.feature_class.repository.ClassRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * ViewModel for displaying and filtering the list of available character classes.
 * <p>
 * Manages the combined list of official (Open5e) and custom classes, providing
 * filtering capabilities based on search query, game system, and source type (Official/Custom).
 * </p>
 */
public class ClassListViewModel extends ViewModel {
    private final LiveData<List<CombinedClass>> allClasses;
    private final MutableLiveData<String> query = new MutableLiveData<>("");
    private final MutableLiveData<String> systemFilter = new MutableLiveData<>("All");
    private final MutableLiveData<String> typeFilter = new MutableLiveData<>("All"); // All, Official, Custom
    
    private final MediatorLiveData<List<CombinedClass>> filteredClasses = new MediatorLiveData<>();

    public ClassListViewModel(ClassRepository repository) {
        allClasses = repository.getCombinedClasses();
        
        // Use a MediatorLiveData to respond to any of the filter sources or the data source itself
        filteredClasses.addSource(allClasses, list -> updateFilters());
        filteredClasses.addSource(query, q -> updateFilters());
        filteredClasses.addSource(systemFilter, sys -> updateFilters());
        filteredClasses.addSource(typeFilter, type -> updateFilters());
    }

    private void updateFilters() {
        List<CombinedClass> list = allClasses.getValue();
        String q = query.getValue();
        String sys = systemFilter.getValue();
        String type = typeFilter.getValue();

        if (list == null) {
            filteredClasses.setValue(new ArrayList<>());
            return;
        }

        List<CombinedClass> filtered = list.stream()
            .filter(c -> q == null || q.isEmpty() || c.name.toLowerCase().contains(q.toLowerCase()))
            .filter(c -> sys == null || sys.equals("All") || (c.getGameSystem() != null && c.getGameSystem().contains(sys)))
            .filter(c -> {
                if (type == null || type.equals("All")) return true;
                if (type.equals("Custom")) return c.isCustom;
                if (type.equals("Official")) return !c.isCustom;
                return true;
            })
            .collect(Collectors.toList());

        filteredClasses.setValue(filtered);
    }

    public LiveData<List<CombinedClass>> getFilteredClasses() { return filteredClasses; }
    public void setQuery(String q) { query.setValue(q); }
    public void setSystemFilter(String sys) { systemFilter.setValue(sys); }
    public void setTypeFilter(String type) { typeFilter.setValue(type); }

    public static class Factory implements ViewModelProvider.Factory {
        private final ClassRepository repository;
        public Factory(ClassRepository repository) { this.repository = repository; }
        @NonNull @Override public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            return (T) new ClassListViewModel(repository);
        }
    }
}
