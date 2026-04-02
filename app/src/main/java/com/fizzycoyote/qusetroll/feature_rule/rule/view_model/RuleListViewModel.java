package com.fizzycoyote.qusetroll.feature_rule.rule.view_model;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.fizzycoyote.qusetroll.core.models.open5e.rule.RuleDao;
import com.fizzycoyote.qusetroll.core.models.open5e.rule.RuleEntity;

import java.util.List;

public class RuleListViewModel extends ViewModel {
    private final RuleDao ruleDao;
    private final String rulesetKey;
    private final LiveData<List<RuleEntity>> rules;

    public RuleListViewModel(RuleDao ruleDao, String rulesetKey) {
        this.ruleDao = ruleDao;
        this.rulesetKey = rulesetKey;
        rules = ruleDao.getByRuleset(rulesetKey);
    }

    public LiveData<List<RuleEntity>> getRules() {
        return rules;
    }

    public static class Factory extends ViewModelProvider.NewInstanceFactory {
        private final RuleDao ruleDao;
        private final String rulesetKey;

        public Factory(RuleDao ruleDao, String rulesetKey) {
            this.ruleDao = ruleDao;
            this.rulesetKey = rulesetKey;
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            return (T) new RuleListViewModel(ruleDao, rulesetKey);
        }
    }
}