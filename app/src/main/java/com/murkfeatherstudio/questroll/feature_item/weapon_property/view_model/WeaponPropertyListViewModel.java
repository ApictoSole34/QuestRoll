package com.murkfeatherstudio.questroll.feature_item.weapon_property.view_model;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.murkfeatherstudio.questroll.core.models.custom.custom_weapon_property.CustomWeaponPropertyDao;
import com.murkfeatherstudio.questroll.core.models.custom.custom_weapon_property.CustomWeaponPropertyEntity;
import com.murkfeatherstudio.questroll.core.models.open5e.weapon_property.WeaponPropertyDao;
import com.murkfeatherstudio.questroll.core.models.open5e.weapon_property.WeaponPropertyEntity;
import com.murkfeatherstudio.questroll.feature_item.weapon_property.model.CombinedWeaponProperty;

import java.util.ArrayList;
import java.util.List;

public class WeaponPropertyListViewModel extends ViewModel {
    private final WeaponPropertyDao apiDao;
    private final CustomWeaponPropertyDao customDao;
    private final MutableLiveData<String> query = new MutableLiveData<>("");
    private final LiveData<List<CombinedWeaponProperty>> combined;

    public WeaponPropertyListViewModel(WeaponPropertyDao apiDao, CustomWeaponPropertyDao customDao) {
        this.apiDao = apiDao;
        this.customDao = customDao;

        LiveData<List<WeaponPropertyEntity>> apiProps = Transformations.switchMap(query, q ->
                apiDao.getAll()
        );
        LiveData<List<CustomWeaponPropertyEntity>> customProps = customDao.getAll();

        MediatorLiveData<List<CombinedWeaponProperty>> mediator = new MediatorLiveData<>();
        mediator.addSource(apiProps, api -> combine(api, customProps.getValue(), mediator));
        mediator.addSource(customProps, custom -> combine(apiProps.getValue(), custom, mediator));
        combined = mediator;
    }

    private void combine(List<WeaponPropertyEntity> api, List<CustomWeaponPropertyEntity> custom,
                         MediatorLiveData<List<CombinedWeaponProperty>> mediator) {
        List<CombinedWeaponProperty> result = new ArrayList<>();
        String currentQuery = query.getValue();
        if (currentQuery == null) currentQuery = "";

        if (api != null) {
            for (WeaponPropertyEntity a : api) {
                if (currentQuery.isEmpty() || a.name.toLowerCase().contains(currentQuery)) {
                    result.add(new CombinedWeaponProperty(a));
                }
            }
        }
        if (custom != null) {
            for (CustomWeaponPropertyEntity c : custom) {
                if (currentQuery.isEmpty() || c.name.toLowerCase().contains(currentQuery)) {
                    result.add(new CombinedWeaponProperty(c));
                }
            }
        }
        result.sort((a, b) -> a.name.compareTo(b.name));
        mediator.setValue(result);
    }

    public void setQuery(String q) { query.setValue(q); }
    public LiveData<List<CombinedWeaponProperty>> getProperties() { return combined; }

    public static class Factory extends ViewModelProvider.NewInstanceFactory {
        private final WeaponPropertyDao apiDao;
        private final CustomWeaponPropertyDao customDao;
        public Factory(WeaponPropertyDao apiDao, CustomWeaponPropertyDao customDao) {
            this.apiDao = apiDao; this.customDao = customDao;
        }
        @NonNull @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            return (T) new WeaponPropertyListViewModel(apiDao, customDao);
        }
    }
}