package com.fizzycoyote.qusetroll.feature_damage_types.view_model;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.fizzycoyote.qusetroll.core.models.custom.custom_damage_types.CustomDamageTypeDao;
import com.fizzycoyote.qusetroll.core.models.custom.custom_damage_types.CustomDamageTypeEntity;
import com.fizzycoyote.qusetroll.core.models.open5e.damage_type.DamageTypeDao;
import com.fizzycoyote.qusetroll.core.models.open5e.damage_type.DamageTypeEntity;
import com.fizzycoyote.qusetroll.feature_damage_types.model.CombinedDamageType;
import com.fizzycoyote.qusetroll.feature_damage_types.model.DamageTypeFilter;

import java.util.ArrayList;
import java.util.List;

public class DamageTypeListViewModel extends ViewModel {
    private final DamageTypeDao damageTypeDao;
    private final CustomDamageTypeDao customDamageTypeDao;

    private final MutableLiveData<DamageTypeFilter> filter = new MutableLiveData<>(new DamageTypeFilter());
    private final LiveData<List<CombinedDamageType>> combined;

    public DamageTypeListViewModel(DamageTypeDao damageTypeDao, CustomDamageTypeDao customDamageTypeDao) {
        this.damageTypeDao = damageTypeDao;
        this.customDamageTypeDao = customDamageTypeDao;

        LiveData<List<DamageTypeEntity>> api = Transformations.switchMap(filter, f ->
                damageTypeDao.getAll()
        );

        LiveData<List<CustomDamageTypeEntity>> custom = customDamageTypeDao.getAll();

        MediatorLiveData<List<CombinedDamageType>> mediator = new MediatorLiveData<>();
        mediator.addSource(api, apiList -> combine(apiList, custom.getValue(), mediator));
        mediator.addSource(custom, customList -> combine(api.getValue(), customList, mediator));
        combined = mediator;
    }

    private void combine(List<DamageTypeEntity> api, List<CustomDamageTypeEntity> custom,
                         MediatorLiveData<List<CombinedDamageType>> mediator) {
        List<CombinedDamageType> result = new ArrayList<>();
        DamageTypeFilter f = filter.getValue();
        boolean customOnly = f != null && f.customOnly;

        if (!customOnly && api != null) {
            for (DamageTypeEntity t : api) {
                if (f != null && !f.query.isEmpty() && !t.name.toLowerCase().contains(f.query.toLowerCase())) continue;
                result.add(new CombinedDamageType(t));
            }
        }
        if (custom != null) {
            for (CustomDamageTypeEntity t : custom) {
                if (f != null && !f.query.isEmpty() && !t.name.toLowerCase().contains(f.query.toLowerCase())) continue;
                result.add(new CombinedDamageType(t));
            }
        }
        result.sort((a, b) -> a.name.compareTo(b.name));
        mediator.setValue(result);
    }

    public void setQuery(String q) {
        DamageTypeFilter current = filter.getValue();
        if (current == null) current = new DamageTypeFilter();
        DamageTypeFilter f = new DamageTypeFilter();
        f.query = q != null ? q : "";
        f.customOnly = current.customOnly;
        filter.setValue(f);
    }

    public void setCustomOnly(boolean customOnly) {
        DamageTypeFilter current = filter.getValue();
        if (current == null) current = new DamageTypeFilter();
        DamageTypeFilter f = new DamageTypeFilter();
        f.query = current.query;
        f.customOnly = customOnly;
        filter.setValue(f);
    }

    public void clearFilters() {
        filter.setValue(new DamageTypeFilter());
    }

    public LiveData<List<CombinedDamageType>> getDamageTypes() { return combined; }
    public LiveData<DamageTypeFilter> getFilter() { return filter; }

    public static class Factory extends ViewModelProvider.NewInstanceFactory {
        private final DamageTypeDao damageTypeDao;
        private final CustomDamageTypeDao customDamageTypeDao;

        public Factory(DamageTypeDao damageTypeDao, CustomDamageTypeDao customDamageTypeDao) {
            this.damageTypeDao = damageTypeDao;
            this.customDamageTypeDao = customDamageTypeDao;
        }

        @NonNull @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            return (T) new DamageTypeListViewModel(damageTypeDao, customDamageTypeDao);
        }
    }
}