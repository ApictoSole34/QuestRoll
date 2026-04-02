package com.fizzycoyote.qusetroll.feature_environment.view_model;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.fizzycoyote.qusetroll.core.models.custom.custom_environment.CustomEnvironmentDao;
import com.fizzycoyote.qusetroll.core.models.custom.custom_environment.CustomEnvironmentEntity;
import com.fizzycoyote.qusetroll.core.models.open5e.environment.EnvironmentDao;
import com.fizzycoyote.qusetroll.core.models.open5e.environment.EnvironmentEntity;
import com.fizzycoyote.qusetroll.feature_environment.model.CombinedEnvironment;

import java.util.ArrayList;
import java.util.List;

public class EnvironmentListViewModel extends ViewModel {
    private final EnvironmentDao apiDao;
    private final CustomEnvironmentDao customDao;
    private final MutableLiveData<String> query = new MutableLiveData<>("");
    private final LiveData<List<CombinedEnvironment>> combined;

    public EnvironmentListViewModel(EnvironmentDao apiDao, CustomEnvironmentDao customDao) {
        this.apiDao = apiDao;
        this.customDao = customDao;
        LiveData<List<EnvironmentEntity>> apiEnvs = Transformations.switchMap(query, q -> apiDao.getAll());
        LiveData<List<CustomEnvironmentEntity>> customEnvs = customDao.getAll();
        MediatorLiveData<List<CombinedEnvironment>> mediator = new MediatorLiveData<>();
        mediator.addSource(apiEnvs, api -> combine(api, customEnvs.getValue(), mediator));
        mediator.addSource(customEnvs, custom -> combine(apiEnvs.getValue(), custom, mediator));
        combined = mediator;
    }

    private void combine(List<EnvironmentEntity> api, List<CustomEnvironmentEntity> custom,
                         MediatorLiveData<List<CombinedEnvironment>> mediator) {
        List<CombinedEnvironment> result = new ArrayList<>();
        String q = query.getValue() == null ? "" : query.getValue().toLowerCase();
        if (api != null) {
            for (EnvironmentEntity a : api) {
                if (q.isEmpty() || a.name.toLowerCase().contains(q))
                    result.add(new CombinedEnvironment(a));
            }
        }
        if (custom != null) {
            for (CustomEnvironmentEntity c : custom) {
                if (q.isEmpty() || c.name.toLowerCase().contains(q))
                    result.add(new CombinedEnvironment(c));
            }
        }
        result.sort((a,b) -> a.name.compareTo(b.name));
        mediator.setValue(result);
    }

    public void setQuery(String q) { query.setValue(q); }
    public LiveData<List<CombinedEnvironment>> getEnvironments() { return combined; }

    public static class Factory extends ViewModelProvider.NewInstanceFactory {
        private final EnvironmentDao apiDao;
        private final CustomEnvironmentDao customDao;
        public Factory(EnvironmentDao apiDao, CustomEnvironmentDao customDao) {
            this.apiDao = apiDao; this.customDao = customDao;
        }
        @NonNull @Override public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            return (T) new EnvironmentListViewModel(apiDao, customDao);
        }
    }
}
