package com.fizzycoyote.qusetroll.feature_species.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.fizzycoyote.qusetroll.core.models.custom.custom_species.CustomSpeciesDao;
import com.fizzycoyote.qusetroll.core.models.custom.custom_species.CustomSpeciesEntity;
import com.fizzycoyote.qusetroll.core.models.open5e.species.SpeciesDao;
import com.fizzycoyote.qusetroll.core.models.open5e.species.SpeciesEntity;
import com.fizzycoyote.qusetroll.feature_species.model.CombinedSpecies;
import com.fizzycoyote.qusetroll.feature_species.model.SpeciesFilter;

import java.util.ArrayList;
import java.util.List;

/**
 * ViewModel for the Species List screen, providing a searchable and filterable list of
 * species (races) and subspecies from both the official compendium and custom user content.
 * <p>
 * It handles logic for distinguishing between main species and subspecies, and supports
 * filtering by search query and data source.
 * </p>
 */
public class SpeciesListViewModel extends ViewModel {

    private final SpeciesDao speciesDao;
    private final CustomSpeciesDao customSpeciesDao;

    private final MutableLiveData<SpeciesFilter> filter = new MutableLiveData<>(new SpeciesFilter());
    private final LiveData<List<CombinedSpecies>> combinedSpecies;
    private final MutableLiveData<List<String>> sources = new MutableLiveData<>();

    public SpeciesListViewModel(SpeciesDao speciesDao, CustomSpeciesDao customSpeciesDao) {
        this.speciesDao = speciesDao;
        this.customSpeciesDao = customSpeciesDao;

        LiveData<List<SpeciesEntity>> open5eSpecies = Transformations.switchMap(filter, f ->
                speciesDao.getFilteredSpecies(
                        f.query,
                        f.subspeciesOnly ? 1 : 0,
                        f.mainOnly ? 1 : 0,
                        f.source
                )
        );

        LiveData<List<CustomSpeciesEntity>> customSpecies = customSpeciesDao.getAll();

        MediatorLiveData<List<CombinedSpecies>> mediator = new MediatorLiveData<>();
        mediator.addSource(open5eSpecies, o5e ->
                combine(o5e, customSpecies.getValue(), mediator));
        mediator.addSource(customSpecies, custom ->
                combine(open5eSpecies.getValue(), custom, mediator));
        combinedSpecies = mediator;
    }

    /**
     * Merges official and custom species into a single sorted list, applying the current
     * filter criteria for subspecies vs main species.
     */
    private void combine(List<SpeciesEntity> open5e,
                         List<CustomSpeciesEntity> custom,
                         MediatorLiveData<List<CombinedSpecies>> result) {
        List<CombinedSpecies> combined = new ArrayList<>();
        SpeciesFilter f = filter.getValue();
        boolean sourceIsCustom = f != null && f.source.equals("custom");

        if (custom != null) {
            for (CustomSpeciesEntity s : custom) {
                if (f != null && !f.source.isEmpty() && !f.source.equals("custom")) continue;
                if (f != null && !f.query.isEmpty()
                        && !s.name.toLowerCase().contains(f.query.toLowerCase())) continue;
                if (f != null && f.subspeciesOnly && !s.isSubspecies) continue;
                if (f != null && f.mainOnly && s.isSubspecies) continue;
                combined.add(new CombinedSpecies(s));
            }
        }

        if (open5e != null && !sourceIsCustom) {
            for (SpeciesEntity s : open5e) combined.add(new CombinedSpecies(s));
        }

        combined.sort((a, b) -> a.name.compareTo(b.name));
        result.setValue(combined);
    }

    /**
     * Loads available data sources for species from the database and includes "custom".
     */
    public void loadSources() {
        new Thread(() -> {
            List<String> dbSources = new ArrayList<>(speciesDao.getDistinctSources());
            dbSources.add(0, "custom");
            sources.postValue(dbSources);
        }).start();
    }

    public LiveData<List<CombinedSpecies>> getSpecies() { return combinedSpecies; }
    public LiveData<SpeciesFilter> getFilter() { return filter; }
    public LiveData<List<String>> getSources() { return sources; }

    public void setQuery(String q) {
        SpeciesFilter old = filter.getValue();
        SpeciesFilter f = new SpeciesFilter();
        if (old != null) {
            f.subspeciesOnly = old.subspeciesOnly;
            f.mainOnly = old.mainOnly;
            f.source = old.source;
        }
        f.query = q != null ? q : "";
        filter.setValue(f);
    }

    public void setSubspeciesOnly(boolean v) {
        SpeciesFilter old = filter.getValue();
        SpeciesFilter f = new SpeciesFilter();
        if (old != null) {
            f.query = old.query;
            f.source = old.source;
        }
        f.subspeciesOnly = v;
        f.mainOnly = false;
        filter.setValue(f);
    }

    public void setMainOnly(boolean v) {
        SpeciesFilter old = filter.getValue();
        SpeciesFilter f = new SpeciesFilter();
        if (old != null) {
            f.query = old.query;
            f.source = old.source;
        }
        f.mainOnly = v;
        f.subspeciesOnly = false;
        filter.setValue(f);
    }

    public void setSource(String s) {
        SpeciesFilter old = filter.getValue();
        SpeciesFilter f = new SpeciesFilter();
        if (old != null) {
            f.query = old.query;
            f.subspeciesOnly = old.subspeciesOnly;
            f.mainOnly = old.mainOnly;
        }
        f.source = s != null ? s : "";
        filter.setValue(f);
    }
    public void clearFilters() { filter.setValue(new SpeciesFilter()); }

    /**
     * Applies specific filters for subspecies vs base species.
     *
     * @param subspeciesOnly Show only subspecies.
     * @param mainOnly       Show only base species.
     */
    public void applySubspeciesFilter(boolean subspeciesOnly, boolean mainOnly) {
        SpeciesFilter old = filter.getValue();
        SpeciesFilter f = new SpeciesFilter();
        if (old != null) { f.query = old.query; f.source = old.source; }
        f.subspeciesOnly = subspeciesOnly;
        f.mainOnly = mainOnly;
        filter.setValue(f);
    }

    private SpeciesFilter get() {
        SpeciesFilter f = filter.getValue();
        return f != null ? f : new SpeciesFilter();
    }

    /**
     * Factory for creating {@link SpeciesListViewModel} with its required DAOs.
     */
    public static class Factory extends ViewModelProvider.NewInstanceFactory {
        private final SpeciesDao speciesDao;
        private final CustomSpeciesDao customSpeciesDao;

        public Factory(SpeciesDao speciesDao, CustomSpeciesDao customSpeciesDao) {
            this.speciesDao = speciesDao;
            this.customSpeciesDao = customSpeciesDao;
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            return (T) new SpeciesListViewModel(speciesDao, customSpeciesDao);
        }
    }
}
