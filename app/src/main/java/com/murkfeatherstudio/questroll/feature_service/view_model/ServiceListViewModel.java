package com.murkfeatherstudio.questroll.feature_service.view_model;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.murkfeatherstudio.questroll.core.models.custom.custom_service.CustomServiceDao;
import com.murkfeatherstudio.questroll.core.models.custom.custom_service.CustomServiceEntity;
import com.murkfeatherstudio.questroll.core.models.open5e.service.ServiceDao;
import com.murkfeatherstudio.questroll.core.models.open5e.service.ServiceEntity;
import com.murkfeatherstudio.questroll.feature_service.model.CombinedService;

import java.util.ArrayList;
import java.util.List;

public class ServiceListViewModel extends ViewModel {
    private final ServiceDao apiDao;
    private final CustomServiceDao customDao;
    private final MutableLiveData<String> query = new MutableLiveData<>("");
    private final LiveData<List<CombinedService>> combined;

    public ServiceListViewModel(ServiceDao apiDao, CustomServiceDao customDao) {
        this.apiDao = apiDao;
        this.customDao = customDao;

        LiveData<List<ServiceEntity>> apiServices = Transformations.switchMap(query, q ->
                apiDao.getAll()
        );
        LiveData<List<CustomServiceEntity>> customServices = customDao.getAll();

        MediatorLiveData<List<CombinedService>> mediator = new MediatorLiveData<>();
        mediator.addSource(apiServices, api -> combine(api, customServices.getValue(), mediator));
        mediator.addSource(customServices, custom -> combine(apiServices.getValue(), custom, mediator));
        combined = mediator;
    }

    private void combine(List<ServiceEntity> api, List<CustomServiceEntity> custom,
                         MediatorLiveData<List<CombinedService>> mediator) {
        List<CombinedService> result = new ArrayList<>();
        String currentQuery = query.getValue();
        if (currentQuery == null) currentQuery = "";

        if (api != null) {
            for (ServiceEntity a : api) {
                if (currentQuery.isEmpty() || a.name.toLowerCase().contains(currentQuery)) {
                    result.add(new CombinedService(a));
                }
            }
        }
        if (custom != null) {
            for (CustomServiceEntity c : custom) {
                if (currentQuery.isEmpty() || c.name.toLowerCase().contains(currentQuery)) {
                    result.add(new CombinedService(c));
                }
            }
        }
        result.sort((a, b) -> a.name.compareTo(b.name));
        mediator.setValue(result);
    }

    public void setQuery(String q) { query.setValue(q); }
    public LiveData<List<CombinedService>> getServices() { return combined; }

    public static class Factory extends ViewModelProvider.NewInstanceFactory {
        private final ServiceDao apiDao;
        private final CustomServiceDao customDao;
        public Factory(ServiceDao apiDao, CustomServiceDao customDao) {
            this.apiDao = apiDao; this.customDao = customDao;
        }
        @NonNull @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            return (T) new ServiceListViewModel(apiDao, customDao);
        }
    }
}
