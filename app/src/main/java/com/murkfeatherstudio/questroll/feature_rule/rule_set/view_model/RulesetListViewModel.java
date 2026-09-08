package com.murkfeatherstudio.questroll.feature_rule.rule_set.view_model;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.murkfeatherstudio.questroll.core.models.open5e.rule_set.RulesetDao;
import com.murkfeatherstudio.questroll.core.models.open5e.rule_set.RulesetEntity;

import java.util.List;

public class RulesetListViewModel extends ViewModel {
    private final RulesetDao dao;
    private final MutableLiveData<String> query = new MutableLiveData<>("");
    private final MutableLiveData<String> selectedSource = new MutableLiveData<>("");
    private final LiveData<List<RulesetEntity>> filteredRulesets;
    private final LiveData<List<String>> sources;

    public RulesetListViewModel(RulesetDao dao) {
        this.dao = dao;
        MediatorLiveData<Object> trigger = new MediatorLiveData<>();
        trigger.addSource(query, v -> trigger.setValue(v));
        trigger.addSource(selectedSource, v -> trigger.setValue(v));

        filteredRulesets = Transformations.switchMap(trigger, ignored ->
                dao.getFiltered(
                        query.getValue() == null ? "" : query.getValue(),
                        selectedSource.getValue() == null ? "" : selectedSource.getValue())
        );
        sources = dao.getDistinctSources();
    }

    public void setQuery(String q) { query.setValue(q); }
    public void setSelectedSource(String source) { selectedSource.setValue(source); }
    public LiveData<List<RulesetEntity>> getFilteredRulesets() { return filteredRulesets; }
    public LiveData<List<String>> getSources() { return sources; }

    public static class Factory extends ViewModelProvider.NewInstanceFactory {
        private final RulesetDao dao;
        public Factory(RulesetDao dao) { this.dao = dao; }
        @NonNull @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            return (T) new RulesetListViewModel(dao);
        }
    }
}