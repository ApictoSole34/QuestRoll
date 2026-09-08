package com.murkfeatherstudio.questroll.feature_creature.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.murkfeatherstudio.questroll.core.AppExecutors;
import com.murkfeatherstudio.questroll.core.models.custom.custom_creature_type.CustomCreatureTypeDao;
import com.murkfeatherstudio.questroll.core.models.custom.custom_creature_type.CustomCreatureTypeEntity;
import com.murkfeatherstudio.questroll.core.models.open5e.creature_type.CreatureTypeDao;
import com.murkfeatherstudio.questroll.core.models.open5e.creature_type.CreatureTypeEntity;
import com.murkfeatherstudio.questroll.core.models.custom.custom_creature.CustomCreatureDao;
import com.murkfeatherstudio.questroll.core.models.custom.custom_creature.CustomCreatureEntity;
import com.murkfeatherstudio.questroll.core.models.open5e.creature.CreatureDao;
import com.murkfeatherstudio.questroll.core.models.open5e.creature.CreatureEntity;
import com.murkfeatherstudio.questroll.feature_creature.model.CombinedCreature;
import com.murkfeatherstudio.questroll.feature_creature.model.CreatureFilter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * ViewModel for the Creature List screen, providing a searchable and filterable list of
 * creatures (monsters) from both the official compendium and custom user content.
 * <p>
 * It aggregates creature types from both sources to populate filter options and
 * performs real-time merging and filtering of creature data.
 * </p>
 */
public class CreatureListViewModel extends ViewModel {

    private final CreatureDao creatureDao;
    private final CustomCreatureDao customCreatureDao;
    private final CustomCreatureTypeDao customCreatureTypeDao;
    private final CreatureTypeDao creatureTypeDao;

    private final MutableLiveData<CreatureFilter> filter = new MutableLiveData<>(new CreatureFilter());
    private final LiveData<List<CombinedCreature>> combinedCreatures;
    private final LiveData<List<String>> allTypeNames;
    private final MutableLiveData<List<String>> sources = new MutableLiveData<>();

    public CreatureListViewModel(CreatureDao creatureDao,
                                 CustomCreatureDao customCreatureDao,
                                 CustomCreatureTypeDao customCreatureTypeDao,
                                 CreatureTypeDao creatureTypeDao) {
        this.creatureDao = creatureDao;
        this.customCreatureDao = customCreatureDao;
        this.customCreatureTypeDao = customCreatureTypeDao;
        this.creatureTypeDao = creatureTypeDao;

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

        LiveData<List<CreatureTypeEntity>> apiTypesLive = creatureTypeDao.getAll();
        LiveData<List<CustomCreatureTypeEntity>> customTypesLive = customCreatureTypeDao.getAll();

        MediatorLiveData<List<String>> typeMediator = new MediatorLiveData<>();
        typeMediator.addSource(apiTypesLive, apiTypes ->
                mergeTypeNames(apiTypes, customTypesLive.getValue(), typeMediator));
        typeMediator.addSource(customTypesLive, customTypes ->
                mergeTypeNames(apiTypesLive.getValue(), customTypes, typeMediator));
        allTypeNames = typeMediator;
    }

    /**
     * Merges official and custom creature type names into a single unique list for filtering.
     */
    private void mergeTypeNames(List<CreatureTypeEntity> apiTypes,
                                List<CustomCreatureTypeEntity> customTypes,
                                MediatorLiveData<List<String>> out) {
        List<String> names = new ArrayList<>();
        names.add("");
        if (apiTypes != null) {
            for (CreatureTypeEntity t : apiTypes)
                if (!names.contains(t.name)) names.add(t.name);
        }
        if (customTypes != null) {
            for (CustomCreatureTypeEntity t : customTypes)
                if (!names.contains(t.name)) names.add(t.name);
        }
        if (!names.contains("No Type")) names.add("No Type");
        Collections.sort(names.subList(1, names.size()));
        out.setValue(names);
    }

    /**
     * Combines official and custom creatures, applying filters and sorting by Challenge Rating (CR).
     */
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

    /**
     * Loads distinct data sources from the creature compendium and includes "custom".
     */
    public void loadSources() {
        AppExecutors.getInstance().diskIO().execute(() -> {
            List<String> dbSources = new ArrayList<>(creatureDao.getDistinctSources());
            dbSources.add(0, "custom");
            sources.postValue(dbSources);
        });
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

    /**
     * Factory for creating {@link CreatureListViewModel} with its required DAOs.
     */
    public static class Factory extends ViewModelProvider.NewInstanceFactory {
        private final CreatureDao creatureDao;
        private final CustomCreatureDao customCreatureDao;
        private final CustomCreatureTypeDao customCreatureTypeDao;
        private final CreatureTypeDao creatureTypeDao;

        public Factory(CreatureDao creatureDao,
                       CustomCreatureDao customCreatureDao,
                       CustomCreatureTypeDao customCreatureTypeDao,
                       CreatureTypeDao creatureTypeDao) {
            this.creatureDao = creatureDao;
            this.customCreatureDao = customCreatureDao;
            this.customCreatureTypeDao = customCreatureTypeDao;
            this.creatureTypeDao = creatureTypeDao;
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            return (T) new CreatureListViewModel(creatureDao, customCreatureDao, customCreatureTypeDao, creatureTypeDao);
        }
    }
}
