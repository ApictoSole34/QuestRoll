package com.fizzycoyote.qusetroll.feature_spell.view_model;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.fizzycoyote.qusetroll.core.models.custom.custom_spell.CustomSpellDao;
import com.fizzycoyote.qusetroll.core.models.custom.custom_spell.CustomSpellEntity;
import com.fizzycoyote.qusetroll.core.models.custom.custom_spell.CustomSpellSchoolDao;
import com.fizzycoyote.qusetroll.core.models.custom.custom_spell.CustomSpellSchoolEntity;
import com.fizzycoyote.qusetroll.core.models.open5e.spell.SpellDao;
import com.fizzycoyote.qusetroll.core.models.open5e.spell.SpellEntity;
import com.fizzycoyote.qusetroll.core.models.open5e.spell_school.SpellSchoolDao;
import com.fizzycoyote.qusetroll.core.models.open5e.spell_school.SpellSchoolEntity;
import com.fizzycoyote.qusetroll.feature_spell.model.CombinedSpell;
import com.fizzycoyote.qusetroll.feature_spell.model.SpellFilter;

import java.util.ArrayList;
import java.util.List;

public class SpellListViewModel extends ViewModel {

    private final SpellDao spellDao;
    private final SpellSchoolDao spellSchoolDao;
    private final CustomSpellDao customSpellDao;
    private final CustomSpellSchoolDao customSpellSchoolDao;

    private final MutableLiveData<SpellFilter> filter = new MutableLiveData<>(new SpellFilter());
    private final MutableLiveData<List<SpellSchoolEntity>> schools = new MutableLiveData<>();
    private final MutableLiveData<List<String>> sources = new MutableLiveData<>();
    private final LiveData<List<String>> allSchoolNames;
    private final LiveData<List<CombinedSpell>> combinedSpells;

    public SpellListViewModel(SpellDao spellDao,
                              SpellSchoolDao spellSchoolDao,
                              CustomSpellDao customSpellDao,
                              CustomSpellSchoolDao customSpellSchoolDao) {
        this.spellDao = spellDao;
        this.spellSchoolDao = spellSchoolDao;
        this.customSpellDao = customSpellDao;
        this.customSpellSchoolDao = customSpellSchoolDao;

        LiveData<List<SpellEntity>> open5eSpells = Transformations.switchMap(filter, f ->
                spellDao.getFilteredSpells(
                        f.query, f.level, f.schoolKey,
                        f.ritualOnly, f.concentrationOnly, f.source
                )
        );

        LiveData<List<CustomSpellEntity>> customSpells = customSpellDao.getAll();

        MediatorLiveData<List<CombinedSpell>> mediator = new MediatorLiveData<>();
        mediator.addSource(open5eSpells, open5e ->
                combine(open5e, customSpells.getValue(), mediator));
        mediator.addSource(customSpells, custom ->
                combine(open5eSpells.getValue(), custom, mediator));
        combinedSpells = mediator;

        LiveData<List<CustomSpellSchoolEntity>> customSchoolsLive = customSpellSchoolDao.getAll();
        MediatorLiveData<List<String>> schoolNameMediator = new MediatorLiveData<>();

        Observer<Object> refreshSchoolNames = ignored -> new Thread(() -> {
            List<String> names = new ArrayList<>();
            names.add("");

            List<SpellSchoolEntity> open5eSchools = spellSchoolDao.getAllSchools();
            for (SpellSchoolEntity s : open5eSchools) names.add(s.name);

            List<CustomSpellSchoolEntity> customSchools = customSpellSchoolDao.getAllSync();
            for (CustomSpellSchoolEntity s : customSchools) {
                if (!names.contains(s.name)) names.add(s.name);
            }

            names.add("No School");
            schoolNameMediator.postValue(names);
        }).start();

        schoolNameMediator.addSource(customSchoolsLive,
                s -> refreshSchoolNames.onChanged(null));

        refreshSchoolNames.onChanged(null);

        allSchoolNames = schoolNameMediator;
    }

    private void combine(List<SpellEntity> open5e,
                         List<CustomSpellEntity> custom,
                         MediatorLiveData<List<CombinedSpell>> result) {
        List<CombinedSpell> combined = new ArrayList<>();
        SpellFilter currentFilter = filter.getValue();
        boolean sourceIsCustom = currentFilter != null
                && currentFilter.source.equals("custom");

        if (custom != null) {
            for (CustomSpellEntity spell : custom) {
                if (currentFilter != null && !currentFilter.source.isEmpty()
                        && !currentFilter.source.equals("custom")) continue;
                if (currentFilter != null && !currentFilter.query.isEmpty()
                        && !spell.name.toLowerCase()
                        .contains(currentFilter.query.toLowerCase())) continue;
                if (currentFilter != null && currentFilter.level >= 0
                        && spell.level != currentFilter.level) continue;
                if (currentFilter != null && !currentFilter.schoolKey.isEmpty()
                        && spell.schoolName != null
                        && !spell.schoolName.toLowerCase()
                        .contains(currentFilter.schoolKey.toLowerCase())) continue;
                combined.add(new CombinedSpell(spell));
            }
        }

        if (open5e != null && !sourceIsCustom) {
            for (SpellEntity spell : open5e) {
                combined.add(new CombinedSpell(spell));
            }
        }

        combined.sort((a, b) -> {
            if (a.level != b.level) return Integer.compare(a.level, b.level);
            return a.name.compareTo(b.name);
        });

        result.setValue(combined);
    }

    public LiveData<List<CombinedSpell>> getSpells() { return combinedSpells; }
    public LiveData<SpellFilter> getFilter() { return filter; }
    public LiveData<List<SpellSchoolEntity>> getSchools() { return schools; }
    public LiveData<List<String>> getSources() { return sources; }
    public LiveData<List<String>> getAllSchoolNames() { return allSchoolNames; }

    public void setQuery(String query) {
        SpellFilter c = getCurrentFilter(); c.query = query != null ? query : "";
        filter.setValue(c);
    }

    public void setLevel(int level) {
        SpellFilter c = getCurrentFilter(); c.level = level;
        filter.setValue(c);
    }

    public void setSchool(String schoolKey) {
        SpellFilter c = getCurrentFilter(); c.schoolKey = schoolKey != null ? schoolKey : "";
        filter.setValue(c);
    }

    public void setRitualOnly(boolean v) {
        SpellFilter c = getCurrentFilter(); c.ritualOnly = v; filter.setValue(c);
    }

    public void setConcentrationOnly(boolean v) {
        SpellFilter c = getCurrentFilter(); c.concentrationOnly = v; filter.setValue(c);
    }

    public void setSource(String source) {
        SpellFilter c = getCurrentFilter(); c.source = source != null ? source : "";
        filter.setValue(c);
    }

    public void clearFilters() { filter.setValue(new SpellFilter()); }

    public void loadSchools() {
        new Thread(() -> schools.postValue(spellSchoolDao.getAllSchools())).start();
    }

    public void loadSources() {
        new Thread(() -> {
            List<String> dbSources = new ArrayList<>(spellDao.getDistinctSources());
            dbSources.add(0, "custom");
            sources.postValue(dbSources);
        }).start();
    }

    private SpellFilter getCurrentFilter() {
        SpellFilter c = filter.getValue();
        return c != null ? c : new SpellFilter();
    }

    public static class Factory extends ViewModelProvider.NewInstanceFactory {
        private final SpellDao spellDao;
        private final SpellSchoolDao spellSchoolDao;
        private final CustomSpellDao customSpellDao;
        private final CustomSpellSchoolDao customSpellSchoolDao;


        public Factory(SpellDao spellDao, SpellSchoolDao spellSchoolDao,
                       CustomSpellDao customSpellDao, CustomSpellSchoolDao customSpellSchoolDao) {
            this.spellDao = spellDao;
            this.spellSchoolDao = spellSchoolDao;
            this.customSpellDao = customSpellDao;
            this.customSpellSchoolDao = customSpellSchoolDao;
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            return (T) new SpellListViewModel(spellDao, spellSchoolDao, customSpellDao, customSpellSchoolDao);
        }
    }
}