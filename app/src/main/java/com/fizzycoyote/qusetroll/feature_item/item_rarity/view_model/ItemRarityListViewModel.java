package com.fizzycoyote.qusetroll.feature_item.item_rarity.view_model;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.fizzycoyote.qusetroll.core.models.custom.custom_item_rarity.CustomItemRarityDao;
import com.fizzycoyote.qusetroll.core.models.custom.custom_item_rarity.CustomItemRarityEntity;
import com.fizzycoyote.qusetroll.core.models.open5e.item_rarity.ItemRarityDao;
import com.fizzycoyote.qusetroll.core.models.open5e.item_rarity.ItemRarityEntity;
import com.fizzycoyote.qusetroll.feature_item.item_rarity.model.CombinedItemRarity;

import java.util.ArrayList;
import java.util.List;

public class ItemRarityListViewModel extends ViewModel {
    private final ItemRarityDao apiDao;
    private final CustomItemRarityDao customDao;
    private final MutableLiveData<String> query = new MutableLiveData<>("");
    private final LiveData<List<CombinedItemRarity>> combined;

    public ItemRarityListViewModel(ItemRarityDao apiDao, CustomItemRarityDao customDao) {
        this.apiDao = apiDao;
        this.customDao = customDao;

        LiveData<List<ItemRarityEntity>> apiRarities = Transformations.switchMap(query, q ->
                apiDao.getAll()  // filtrowanie w combine
        );
        LiveData<List<CustomItemRarityEntity>> customRarities = customDao.getAll();

        MediatorLiveData<List<CombinedItemRarity>> mediator = new MediatorLiveData<>();
        mediator.addSource(apiRarities, api -> combine(api, customRarities.getValue(), mediator));
        mediator.addSource(customRarities, custom -> combine(apiRarities.getValue(), custom, mediator));
        combined = mediator;
    }

    private void combine(List<ItemRarityEntity> api, List<CustomItemRarityEntity> custom,
                         MediatorLiveData<List<CombinedItemRarity>> mediator) {
        List<CombinedItemRarity> result = new ArrayList<>();
        String currentQuery = query.getValue();
        if (currentQuery == null) currentQuery = "";

        if (api != null) {
            for (ItemRarityEntity a : api) {
                if (currentQuery.isEmpty() || a.name.toLowerCase().contains(currentQuery)) {
                    result.add(new CombinedItemRarity(a));
                }
            }
        }
        if (custom != null) {
            for (CustomItemRarityEntity c : custom) {
                if (currentQuery.isEmpty() || c.name.toLowerCase().contains(currentQuery)) {
                    result.add(new CombinedItemRarity(c));
                }
            }
        }
        result.sort((a, b) -> {
            if (a.rank != b.rank) return Integer.compare(a.rank, b.rank);
            return a.name.compareTo(b.name);
        });
        mediator.setValue(result);
    }

    public void setQuery(String q) {
        query.setValue(q);
    }

    public LiveData<List<CombinedItemRarity>> getRarities() {
        return combined;
    }

    public static class Factory extends ViewModelProvider.NewInstanceFactory {
        private final ItemRarityDao apiDao;
        private final CustomItemRarityDao customDao;

        public Factory(ItemRarityDao apiDao, CustomItemRarityDao customDao) {
            this.apiDao = apiDao;
            this.customDao = customDao;
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            return (T) new ItemRarityListViewModel(apiDao, customDao);
        }
    }
}
