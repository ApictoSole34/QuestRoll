package com.fizzycoyote.qusetroll.feature_item.item_category.view_model;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import com.fizzycoyote.qusetroll.core.models.custom.custom_item_category.CustomItemCategoryDao;
import com.fizzycoyote.qusetroll.core.models.custom.custom_item_category.CustomItemCategoryEntity;
import com.fizzycoyote.qusetroll.core.models.open5e.item_category.ItemCategoryDao;
import com.fizzycoyote.qusetroll.core.models.open5e.item_category.ItemCategoryEntity;
import com.fizzycoyote.qusetroll.feature_item.item_category.model.CombinedItemCategory;

import java.util.ArrayList;
import java.util.List;

public class ItemCategoryListViewModel extends ViewModel {
    private final ItemCategoryDao apiDao;
    private final CustomItemCategoryDao customDao;
    private final MutableLiveData<String> query = new MutableLiveData<>("");
    private final LiveData<List<CombinedItemCategory>> combined;

    public ItemCategoryListViewModel(ItemCategoryDao apiDao, CustomItemCategoryDao customDao) {
        this.apiDao = apiDao;
        this.customDao = customDao;

        LiveData<List<ItemCategoryEntity>> apiCats = Transformations.switchMap(query, q -> apiDao.getAll());
        LiveData<List<CustomItemCategoryEntity>> customCats = customDao.getAll();

        MediatorLiveData<List<CombinedItemCategory>> mediator = new MediatorLiveData<>();
        mediator.addSource(apiCats, api -> combine(api, customCats.getValue(), mediator));
        mediator.addSource(customCats, custom -> combine(apiCats.getValue(), custom, mediator));
        combined = mediator;
    }

    private void combine(List<ItemCategoryEntity> api, List<CustomItemCategoryEntity> custom,
                         MediatorLiveData<List<CombinedItemCategory>> mediator) {
        List<CombinedItemCategory> result = new ArrayList<>();
        String q = query.getValue() == null ? "" : query.getValue().toLowerCase();

        if (api != null) {
            for (ItemCategoryEntity a : api) {
                if (q.isEmpty() || a.name.toLowerCase().contains(q))
                    result.add(new CombinedItemCategory(a));
            }
        }
        if (custom != null) {
            for (CustomItemCategoryEntity c : custom) {
                if (q.isEmpty() || c.name.toLowerCase().contains(q))
                    result.add(new CombinedItemCategory(c));
            }
        }
        result.sort((a,b) -> a.name.compareTo(b.name));
        mediator.setValue(result);
    }

    public void setQuery(String q) { query.setValue(q); }
    public LiveData<List<CombinedItemCategory>> getCategories() { return combined; }

    public static class Factory extends ViewModelProvider.NewInstanceFactory {
        private final ItemCategoryDao apiDao;
        private final CustomItemCategoryDao customDao;
        public Factory(ItemCategoryDao apiDao, CustomItemCategoryDao customDao) {
            this.apiDao = apiDao;
            this.customDao = customDao;
        }
        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            return (T) new ItemCategoryListViewModel(apiDao, customDao);
        }
    }
}
