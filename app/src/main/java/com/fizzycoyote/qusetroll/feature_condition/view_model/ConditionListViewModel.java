package com.fizzycoyote.qusetroll.feature_condition.view_model;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.fizzycoyote.qusetroll.core.models.custom.custom_condition.CustomConditionDao;
import com.fizzycoyote.qusetroll.core.models.custom.custom_condition.CustomConditionEntity;
import com.fizzycoyote.qusetroll.core.models.open5e.condition.ConditionDao;
import com.fizzycoyote.qusetroll.core.models.open5e.condition.ConditionEntity;
import com.fizzycoyote.qusetroll.feature_condition.model.CombinedCondition;
import java.util.ArrayList;
import java.util.List;

public class ConditionListViewModel extends ViewModel {
    private final ConditionDao apiDao;
    private final CustomConditionDao customDao;
    private final MutableLiveData<String> query = new MutableLiveData<>("");
    private final MutableLiveData<String> selectedSource = new MutableLiveData<>("");
    private final MutableLiveData<Boolean> customOnly = new MutableLiveData<>(false);
    private final LiveData<List<CombinedCondition>> combined;
    private final LiveData<List<String>> sources;

    public ConditionListViewModel(ConditionDao apiDao, CustomConditionDao customDao) {
        this.apiDao = apiDao;
        this.customDao = customDao;

        sources = apiDao.getDistinctSources();

        MediatorLiveData<Object> trigger = new MediatorLiveData<>();
        trigger.addSource(query, v -> trigger.setValue(v));
        trigger.addSource(selectedSource, v -> trigger.setValue(v));
        trigger.addSource(customOnly, v -> trigger.setValue(v));

        LiveData<List<ConditionEntity>> filteredApi = Transformations.switchMap(trigger, ignored ->
                apiDao.getFiltered(
                        query.getValue() == null ? "" : query.getValue(),
                        selectedSource.getValue() == null ? "" : selectedSource.getValue())
        );

        LiveData<List<CustomConditionEntity>> customConditions = customDao.getAll();

        MediatorLiveData<List<CombinedCondition>> mediator = new MediatorLiveData<>();
        mediator.addSource(filteredApi, api -> combine(api, customConditions.getValue(), mediator));
        mediator.addSource(customConditions, custom -> combine(filteredApi.getValue(), custom, mediator));
        combined = mediator;
    }


    private void combine(List<ConditionEntity> api, List<CustomConditionEntity> custom,
                         MediatorLiveData<List<CombinedCondition>> mediator) {
        List<CombinedCondition> result = new ArrayList<>();
        String q = query.getValue() == null ? "" : query.getValue().toLowerCase();
        Boolean onlyCustom = customOnly.getValue() != null && customOnly.getValue();

        if (!onlyCustom && api != null) {
            for (ConditionEntity a : api) {
                if (q.isEmpty() || a.name.toLowerCase().contains(q))
                    result.add(new CombinedCondition(a));
            }
        }
        if (custom != null) {
            for (CustomConditionEntity c : custom) {
                if (q.isEmpty() || c.name.toLowerCase().contains(q))
                    result.add(new CombinedCondition(c));
            }
        }
        result.sort((a,b) -> a.name.compareTo(b.name));
        mediator.setValue(result);
    }

    public void setQuery(String q) { query.setValue(q); }
    public void setSelectedSource(String source) { selectedSource.setValue(source); }
    public void setCustomOnly(boolean value) { customOnly.setValue(value); }
    public LiveData<List<CombinedCondition>> getConditions() { return combined; }
    public LiveData<List<String>> getSources() { return sources; }
    public LiveData<Boolean> getCustomOnly() { return customOnly; }

    public static class Factory extends ViewModelProvider.NewInstanceFactory {
        private final ConditionDao apiDao;
        private final CustomConditionDao customDao;
        public Factory(ConditionDao apiDao, CustomConditionDao customDao) {
            this.apiDao = apiDao;
            this.customDao = customDao;
        }
        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            return (T) new ConditionListViewModel(apiDao, customDao);
        }
    }
}