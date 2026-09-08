package com.murkfeatherstudio.questroll.feature_creature.creature_type.view_model;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.murkfeatherstudio.questroll.core.models.custom.custom_creature_type.CustomCreatureTypeDao;
import com.murkfeatherstudio.questroll.core.models.custom.custom_creature_type.CustomCreatureTypeEntity;
import com.murkfeatherstudio.questroll.core.models.open5e.creature_type.CreatureTypeDao;
import com.murkfeatherstudio.questroll.core.models.open5e.creature_type.CreatureTypeEntity;
import com.murkfeatherstudio.questroll.feature_creature.creature_type.model.CombinedCreatureType;

import java.util.ArrayList;
import java.util.List;

public class CreatureTypeListViewModel extends ViewModel {
    private final CreatureTypeDao apiDao;
    private final CustomCreatureTypeDao customDao;
    private final MutableLiveData<String> query = new MutableLiveData<>("");
    private final MutableLiveData<String> selectedSource = new MutableLiveData<>("");
    private final MutableLiveData<Boolean> customOnly = new MutableLiveData<>(false);
    private final LiveData<List<CombinedCreatureType>> combined;
    private final LiveData<List<String>> sources;

    public CreatureTypeListViewModel(CreatureTypeDao apiDao, CustomCreatureTypeDao customDao) {
        this.apiDao = apiDao;
        this.customDao = customDao;

        sources = apiDao.getDistinctSources();

        MediatorLiveData<Object> trigger = new MediatorLiveData<>();
        trigger.addSource(query, v -> trigger.setValue(v));
        trigger.addSource(selectedSource, v -> trigger.setValue(v));
        trigger.addSource(customOnly, v -> trigger.setValue(v));

        LiveData<List<CreatureTypeEntity>> filteredApi = Transformations.switchMap(trigger, ignored ->
                apiDao.getFiltered(
                        query.getValue() == null ? "" : query.getValue(),
                        selectedSource.getValue() == null ? "" : selectedSource.getValue())
        );

        LiveData<List<CustomCreatureTypeEntity>> customTypes = customDao.getAll();

        MediatorLiveData<List<CombinedCreatureType>> mediator = new MediatorLiveData<>();
        mediator.addSource(filteredApi, api -> combine(api, customTypes.getValue(), mediator));
        mediator.addSource(customTypes, custom -> combine(filteredApi.getValue(), custom, mediator));
        combined = mediator;
    }

    private void combine(List<CreatureTypeEntity> api, List<CustomCreatureTypeEntity> custom,
                         MediatorLiveData<List<CombinedCreatureType>> mediator) {
        List<CombinedCreatureType> result = new ArrayList<>();
        String q = query.getValue() == null ? "" : query.getValue().toLowerCase();
        Boolean onlyCustom = customOnly.getValue() != null && customOnly.getValue();

        if (!onlyCustom && api != null) {
            for (CreatureTypeEntity a : api) {
                if (q.isEmpty() || a.name.toLowerCase().contains(q))
                    result.add(new CombinedCreatureType(a));
            }
        }
        if (custom != null) {
            for (CustomCreatureTypeEntity c : custom) {
                if (q.isEmpty() || c.name.toLowerCase().contains(q))
                    result.add(new CombinedCreatureType(c));
            }
        }
        result.sort((a,b) -> a.name.compareTo(b.name));
        mediator.setValue(result);
    }

    public void setQuery(String q) { query.setValue(q); }
    public void setSelectedSource(String source) { selectedSource.setValue(source); }
    public void setCustomOnly(boolean value) { customOnly.setValue(value); }
    public LiveData<List<CombinedCreatureType>> getCreatureTypes() { return combined; }
    public LiveData<List<String>> getSources() { return sources; }
    public LiveData<Boolean> getCustomOnly() { return customOnly; }

    public static class Factory extends ViewModelProvider.NewInstanceFactory {
        private final CreatureTypeDao apiDao;
        private final CustomCreatureTypeDao customDao;
        public Factory(CreatureTypeDao apiDao, CustomCreatureTypeDao customDao) {
            this.apiDao = apiDao;
            this.customDao = customDao;
        }
        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            return (T) new CreatureTypeListViewModel(apiDao, customDao);
        }
    }
}