package com.fizzycoyote.qusetroll.feature_item.view_model.weapon;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.fizzycoyote.qusetroll.core.models.custom.custom_weapon.CustomWeaponDao;
import com.fizzycoyote.qusetroll.core.models.custom.custom_weapon.CustomWeaponEntity;
import com.fizzycoyote.qusetroll.core.models.open5e.weapon.WeaponDao;
import com.fizzycoyote.qusetroll.core.models.open5e.weapon.WeaponEntity;
import com.fizzycoyote.qusetroll.feature_item.model.weapon.CombinedWeapon;
import com.fizzycoyote.qusetroll.feature_item.model.weapon.WeaponFilter;

import java.util.ArrayList;
import java.util.List;

public class WeaponListViewModel extends ViewModel {

    private final WeaponDao weaponDao;
    private final CustomWeaponDao customWeaponDao;

    private final MutableLiveData<WeaponFilter> filter = new MutableLiveData<>(new WeaponFilter());
    private final LiveData<List<CombinedWeapon>> combinedWeapons;
    private final MutableLiveData<List<String>> sources = new MutableLiveData<>();

    public WeaponListViewModel(WeaponDao weaponDao, CustomWeaponDao customWeaponDao) {
        this.weaponDao = weaponDao;
        this.customWeaponDao = customWeaponDao;

        LiveData<List<WeaponEntity>> open5eWeapons = Transformations.switchMap(filter, f ->
                weaponDao.getFiltered(
                        f.query,
                        f.simpleOnly ? 1 : 0,
                        f.martialOnly ? 1 : 0,
                        f.source
                )
        );

        LiveData<List<CustomWeaponEntity>> customWeapons = customWeaponDao.getAll();

        MediatorLiveData<List<CombinedWeapon>> mediator = new MediatorLiveData<>();
        mediator.addSource(open5eWeapons, o5e ->
                combine(o5e, customWeapons.getValue(), mediator));
        mediator.addSource(customWeapons, custom ->
                combine(open5eWeapons.getValue(), custom, mediator));
        combinedWeapons = mediator;
    }

    private void combine(List<WeaponEntity> open5e,
                         List<CustomWeaponEntity> custom,
                         MediatorLiveData<List<CombinedWeapon>> result) {
        List<CombinedWeapon> combined = new ArrayList<>();
        WeaponFilter f = filter.getValue();
        boolean sourceIsCustom = f != null && f.source.equals("custom");

        if (custom != null) {
            for (CustomWeaponEntity w : custom) {
                if (f != null && !f.source.isEmpty() && !sourceIsCustom) continue;
                if (f != null && !f.query.isEmpty()
                        && !w.name.toLowerCase().contains(f.query.toLowerCase())) continue;
                if (f != null && f.simpleOnly && !w.isSimple) continue;
                if (f != null && f.martialOnly && w.isSimple) continue;
                combined.add(new CombinedWeapon(w));
            }
        }

        if (open5e != null && !sourceIsCustom) {
            for (WeaponEntity w : open5e) combined.add(new CombinedWeapon(w));
        }

        combined.sort((a, b) -> a.name.compareTo(b.name));
        result.setValue(combined);
    }

    public void loadSources() {
        new Thread(() -> {
            List<String> dbSources = new ArrayList<>(weaponDao.getDistinctSources());
            dbSources.add(0, "custom");
            sources.postValue(dbSources);
        }).start();
    }

    public void applyFilter(WeaponFilter newFilter) { filter.setValue(newFilter); }

    public void setQuery(String q) {
        WeaponFilter f = new WeaponFilter();
        WeaponFilter old = filter.getValue();
        if (old != null) { f.simpleOnly = old.simpleOnly; f.martialOnly = old.martialOnly; f.source = old.source; }
        f.query = q != null ? q : "";
        filter.setValue(f);
    }

    public void setSource(String s) {
        WeaponFilter f = new WeaponFilter();
        WeaponFilter old = filter.getValue();
        if (old != null) { f.query = old.query; f.simpleOnly = old.simpleOnly; f.martialOnly = old.martialOnly; }
        f.source = s != null ? s : "";
        filter.setValue(f);
    }

    public void clearFilters() { filter.setValue(new WeaponFilter()); }

    public LiveData<List<CombinedWeapon>> getWeapons() { return combinedWeapons; }
    public LiveData<WeaponFilter> getFilter() { return filter; }
    public LiveData<List<String>> getSources() { return sources; }

    public static class Factory extends ViewModelProvider.NewInstanceFactory {
        private final WeaponDao weaponDao;
        private final CustomWeaponDao customWeaponDao;

        public Factory(WeaponDao weaponDao, CustomWeaponDao customWeaponDao) {
            this.weaponDao = weaponDao;
            this.customWeaponDao = customWeaponDao;
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            return (T) new WeaponListViewModel(weaponDao, customWeaponDao);
        }
    }
}