package com.murkfeatherstudio.questroll.feature_spell.spell_school.view_model;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import com.murkfeatherstudio.questroll.core.models.custom.custom_spell.CustomSpellSchoolDao;
import com.murkfeatherstudio.questroll.core.models.custom.custom_spell.CustomSpellSchoolEntity;
import com.murkfeatherstudio.questroll.core.models.open5e.spell_school.SpellSchoolDao;
import com.murkfeatherstudio.questroll.core.models.open5e.spell_school.SpellSchoolEntity;
import com.murkfeatherstudio.questroll.feature_spell.spell_school.model.CombinedSpellSchool;

import java.util.ArrayList;
import java.util.List;

public class SpellSchoolListViewModel extends ViewModel {
    private final SpellSchoolDao apiDao;
    private final CustomSpellSchoolDao customDao;
    private final MutableLiveData<String> query = new MutableLiveData<>("");
    private final LiveData<List<CombinedSpellSchool>> combined;

    public SpellSchoolListViewModel(SpellSchoolDao apiDao, CustomSpellSchoolDao customDao) {
        this.apiDao = apiDao;
        this.customDao = customDao;

        LiveData<List<SpellSchoolEntity>> apiSchools = Transformations.switchMap(query, q ->
                apiDao.getAllSchoolsLive()
        );

        LiveData<List<CustomSpellSchoolEntity>> customSchools = customDao.getAll();

        MediatorLiveData<List<CombinedSpellSchool>> mediator = new MediatorLiveData<>();
        mediator.addSource(apiSchools, api -> combine(api, customSchools.getValue(), mediator));
        mediator.addSource(customSchools, custom -> combine(apiSchools.getValue(), custom, mediator));
        combined = mediator;
    }

    private void combine(List<SpellSchoolEntity> api, List<CustomSpellSchoolEntity> custom,
                         MediatorLiveData<List<CombinedSpellSchool>> mediator) {
        List<CombinedSpellSchool> result = new ArrayList<>();
        String q = query.getValue() == null ? "" : query.getValue().toLowerCase();

        if (api != null) {
            for (SpellSchoolEntity a : api) {
                if (q.isEmpty() || a.name.toLowerCase().contains(q))
                    result.add(new CombinedSpellSchool(a));
            }
        }
        if (custom != null) {
            for (CustomSpellSchoolEntity c : custom) {
                if (q.isEmpty() || c.name.toLowerCase().contains(q))
                    result.add(new CombinedSpellSchool(c));
            }
        }
        result.sort((a,b) -> a.name.compareTo(b.name));
        mediator.setValue(result);
    }

    public void setQuery(String q) { query.setValue(q); }
    public LiveData<List<CombinedSpellSchool>> getSpellSchools() { return combined; }

    public static class Factory extends ViewModelProvider.NewInstanceFactory {
        private final SpellSchoolDao apiDao;
        private final CustomSpellSchoolDao customDao;
        public Factory(SpellSchoolDao apiDao, CustomSpellSchoolDao customDao) {
            this.apiDao = apiDao;
            this.customDao = customDao;
        }
        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            return (T) new SpellSchoolListViewModel(apiDao, customDao);
        }
    }
}