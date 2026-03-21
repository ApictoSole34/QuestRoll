package com.fizzycoyote.qusetroll.feature_creature.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.fizzycoyote.qusetroll.core.models.custom.custom_creature.CustomCreatureDao;
import com.fizzycoyote.qusetroll.core.models.custom.custom_creature.CustomCreatureEntity;
import com.fizzycoyote.qusetroll.core.models.custom.custom_creature.CustomCreatureTypeDao;
import com.fizzycoyote.qusetroll.core.models.custom.custom_creature.CustomCreatureTypeEntity;
import com.fizzycoyote.qusetroll.core.models.open5e.creature.CreatureDao;
import com.fizzycoyote.qusetroll.core.models.open5e.creature.CreatureEntity;
import com.fizzycoyote.qusetroll.feature_creature.model.CombinedCreature;
import com.fizzycoyote.qusetroll.feature_creature.model.CreatureFilter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CreatureListViewModel extends ViewModel {

    private final CreatureDao creatureDao;
    private final CustomCreatureDao customCreatureDao;
    private final CustomCreatureTypeDao customCreatureTypeDao;

    private final MutableLiveData<CreatureFilter> filter = new MutableLiveData<>(new CreatureFilter());
    private final LiveData<List<CombinedCreature>> combinedCreatures;
    private final LiveData<List<String>> allTypeNames;
    private final MutableLiveData<List<String>> sources = new MutableLiveData<>();

    public CreatureListViewModel(CreatureDao creatureDao,
                                 CustomCreatureDao customCreatureDao,
                                 CustomCreatureTypeDao customCreatureTypeDao) {
        this.creatureDao = creatureDao;
        this.customCreatureDao = customCreatureDao;
        this.customCreatureTypeDao = customCreatureTypeDao;

        LiveData<List<CreatureEntity>> open5eCreatures = Transformations.switchMap(filter, f ->
                creatureDao.getFilteredCreatures(
                        f.query, f.typeKey, f.alignment, f.crMin, f.crMax, f.source));

        LiveData<List<CustomCreatureEntity>> customCreatures = customCreatureDao.getAll();

        MediatorLiveData<List<CombinedCreature>> mediator = new MediatorLiveData<>();
        mediator.addSource(open5eCreatures, o5e ->
                combine(o5e, customCreatures.getValue(), mediator));
        mediator.addSource(customCreatures, custom ->
                combine(open5eCreatures.getValue(), custom, mediator));
        combinedCreatures = mediator;

        LiveData<List<CustomCreatureTypeEntity>> customTypesLive = customCreatureTypeDao.getAll();
        MediatorLiveData<List<String>> typeMediator = new MediatorLiveData<>();
        Observer<Object> refreshTypes = ignored -> new Thread(() -> {
            List<String> names = new ArrayList<>();
            names.add(""); // placeholder
            names.addAll(Arrays.asList(
                    "Aberration", "Beast", "Celestial", "Construct",
                    "Dragon", "Elemental", "Fey", "Fiend", "Giant",
                    "Humanoid", "Monstrosity", "Ooze", "Plant", "Undead",
                    "No Type"
            ));
            for (CustomCreatureTypeEntity t : customCreatureTypeDao.getAllSync()) {
                if (!names.contains(t.name)) names.add(t.name);
            }
            typeMediator.postValue(names);
        }).start();
        typeMediator.addSource(customTypesLive, t -> refreshTypes.onChanged(null));
        refreshTypes.onChanged(null);
        allTypeNames = typeMediator;
    }

    private void combine(List<CreatureEntity> open5e,
                         List<CustomCreatureEntity> custom,
                         MediatorLiveData<List<CombinedCreature>> result) {
        List<CombinedCreature> combined = new ArrayList<>();
        CreatureFilter f = filter.getValue();
        boolean sourceIsCustom = f != null && f.source.equals("custom");

        if (custom != null) {
            for (CustomCreatureEntity c : custom) {
                if (f != null && !f.source.isEmpty() && !f.source.equals("custom")) continue;
                if (f != null && !f.query.isEmpty()
                        && !c.name.toLowerCase().contains(f.query.toLowerCase())) continue;
                if (f != null && !f.typeKey.isEmpty()
                        && !c.typeName.toLowerCase().contains(f.typeKey.toLowerCase())) continue;
                if (f != null && !f.alignment.isEmpty()
                        && (c.alignment == null || !c.alignment.toLowerCase()
                        .contains(f.alignment.toLowerCase()))) continue;
                if (f != null && f.crMin >= 0 && c.crDecimal < f.crMin) continue;
                if (f != null && f.crMax >= 0 && c.crDecimal > f.crMax) continue;
                combined.add(new CombinedCreature(c));
            }
        }

        if (open5e != null && !sourceIsCustom) {
            for (CreatureEntity c : open5e) combined.add(new CombinedCreature(c));
        }

        combined.sort((a, b) -> {
            if (a.crDecimal != b.crDecimal) return Float.compare(a.crDecimal, b.crDecimal);
            return a.name.compareTo(b.name);
        });

        result.setValue(combined);
    }

    public void loadSources() {
        new Thread(() -> {
            List<String> dbSources = new ArrayList<>(creatureDao.getDistinctSources());
            dbSources.add(0, "custom");
            sources.postValue(dbSources);
        }).start();
    }

    public LiveData<List<CombinedCreature>> getCreatures() { return combinedCreatures; }
    public LiveData<CreatureFilter> getFilter() { return filter; }
    public LiveData<List<String>> getAllTypeNames() { return allTypeNames; }
    public LiveData<List<String>> getSources() { return sources; }

    public void setQuery(String q) {
        CreatureFilter c = get(); c.query = q != null ? q : ""; filter.setValue(c);
    }
    public void setTypeKey(String t) {
        CreatureFilter c = get(); c.typeKey = t != null ? t : ""; filter.setValue(c);
    }
    public void setAlignment(String a) {
        CreatureFilter c = get(); c.alignment = a != null ? a : ""; filter.setValue(c);
    }
    public void setCrRange(float min, float max) {
        CreatureFilter c = get(); c.crMin = min; c.crMax = max; filter.setValue(c);
    }
    public void setSource(String s) {
        CreatureFilter c = get(); c.source = s != null ? s : ""; filter.setValue(c);
    }
    public void clearFilters() { filter.setValue(new CreatureFilter()); }

    private CreatureFilter get() {
        CreatureFilter c = filter.getValue();
        return c != null ? c : new CreatureFilter();
    }

    public static class Factory extends ViewModelProvider.NewInstanceFactory {
        private final CreatureDao creatureDao;
        private final CustomCreatureDao customCreatureDao;
        private final CustomCreatureTypeDao customCreatureTypeDao;

        public Factory(CreatureDao creatureDao, CustomCreatureDao customCreatureDao,
                       CustomCreatureTypeDao customCreatureTypeDao) {
            this.creatureDao = creatureDao;
            this.customCreatureDao = customCreatureDao;
            this.customCreatureTypeDao = customCreatureTypeDao;
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            return (T) new CreatureListViewModel(creatureDao, customCreatureDao, customCreatureTypeDao);
        }
    }
}