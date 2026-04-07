package com.fizzycoyote.qusetroll.feature_item.item_set.view_model;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import com.fizzycoyote.qusetroll.core.models.custom.custom_item_set.CustomItemSetDao;
import com.fizzycoyote.qusetroll.core.models.custom.custom_item_set.CustomItemSetEntity;
import com.fizzycoyote.qusetroll.core.models.open5e.item_set.ItemSetDao;
import com.fizzycoyote.qusetroll.core.models.open5e.item_set.ItemSetEntity;
import com.fizzycoyote.qusetroll.feature_item.item_set.model.CombinedItemSet;

import java.util.ArrayList;
import java.util.List;

public class ItemSetListViewModel extends ViewModel {
    private final ItemSetDao apiDao;
    private final CustomItemSetDao customDao;
    private final MutableLiveData<String> query = new MutableLiveData<>("");
    private final LiveData<List<CombinedItemSet>> combined;

    public ItemSetListViewModel(ItemSetDao apiDao, CustomItemSetDao customDao) {
        this.apiDao = apiDao;
        this.customDao = customDao;
        LiveData<List<ItemSetEntity>> apiSets = Transformations.switchMap(query, q -> apiDao.getAll());
        LiveData<List<CustomItemSetEntity>> customSets = customDao.getAll();
        MediatorLiveData<List<CombinedItemSet>> mediator = new MediatorLiveData<>();
        mediator.addSource(apiSets, api -> combine(api, customSets.getValue(), mediator));
        mediator.addSource(customSets, custom -> combine(apiSets.getValue(), custom, mediator));
        combined = mediator;
    }

    private void combine(List<ItemSetEntity> api, List<CustomItemSetEntity> custom,
                         MediatorLiveData<List<CombinedItemSet>> mediator) {
        List<CombinedItemSet> result = new ArrayList<>();
        String q = query.getValue() == null ? "" : query.getValue().toLowerCase();
        if (api != null) {
            for (ItemSetEntity a : api) {
                if (q.isEmpty() || a.name.toLowerCase().contains(q))
                    result.add(new CombinedItemSet(a));
            }
        }
        if (custom != null) {
            for (CustomItemSetEntity c : custom) {
                if (q.isEmpty() || c.name.toLowerCase().contains(q))
                    result.add(new CombinedItemSet(c));
            }
        }
        result.sort((a,b) -> a.name.compareTo(b.name));
        mediator.setValue(result);
    }

    public void setQuery(String q) { query.setValue(q); }
    public LiveData<List<CombinedItemSet>> getItemSets() { return combined; }

    public static class Factory extends ViewModelProvider.NewInstanceFactory {
        private final ItemSetDao apiDao;
        private final CustomItemSetDao customDao;
        public Factory(ItemSetDao apiDao, CustomItemSetDao customDao) {
            this.apiDao = apiDao; this.customDao = customDao;
        }
        @NonNull @Override public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            return (T) new ItemSetListViewModel(apiDao, customDao);
        }
    }
}