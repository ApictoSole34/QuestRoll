package com.fizzycoyote.qusetroll.feature_item.view_model;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.fizzycoyote.qusetroll.core.models.custom.custom_item.CustomItemDao;
import com.fizzycoyote.qusetroll.core.models.custom.custom_item.CustomItemEntity;
import com.fizzycoyote.qusetroll.core.models.open5e.item.ItemDao;
import com.fizzycoyote.qusetroll.core.models.open5e.item.ItemEntity;
import com.fizzycoyote.qusetroll.feature_item.model.CombinedItem;
import com.fizzycoyote.qusetroll.feature_item.model.ItemFilter;

import java.util.ArrayList;
import java.util.List;

public class ItemListViewModel extends ViewModel {

    private final ItemDao itemDao;
    private final CustomItemDao customItemDao;

    private final MutableLiveData<ItemFilter> filter = new MutableLiveData<>(new ItemFilter());
    private final LiveData<List<CombinedItem>> combinedItems;
    private final MutableLiveData<List<String>> sources = new MutableLiveData<>();
    private final MutableLiveData<List<String>> categories = new MutableLiveData<>();

    public ItemListViewModel(ItemDao itemDao, CustomItemDao customItemDao) {
        this.itemDao = itemDao;
        this.customItemDao = customItemDao;

        LiveData<List<ItemEntity>> apiItems = Transformations.switchMap(filter, f ->
                itemDao.getFiltered(
                        f.query,
                        f.categoryName,
                        f.source,
                        f.magicOnly ? 1 : 0,
                        f.rarity,
                        f.requiresAttunement ? 1 : 0
                )
        );

        LiveData<List<CustomItemEntity>> customItems = customItemDao.getAll();

        MediatorLiveData<List<CombinedItem>> mediator = new MediatorLiveData<>();
        mediator.addSource(apiItems, api -> combine(api, customItems.getValue(), mediator));
        mediator.addSource(customItems, custom -> combine(apiItems.getValue(), custom, mediator));
        combinedItems = mediator;
    }

    private void combine(List<ItemEntity> api,
                         List<CustomItemEntity> custom,
                         MediatorLiveData<List<CombinedItem>> result) {
        List<CombinedItem> combined = new ArrayList<>();
        ItemFilter f = filter.getValue();
        boolean sourceIsCustom = f != null && f.source.equals("custom");

        if (custom != null && !sourceIsCustom) {
            for (CustomItemEntity item : custom) {
                if (f != null && !f.query.isEmpty()
                        && !item.name.toLowerCase().contains(f.query.toLowerCase())) continue;
                if (f != null && !f.categoryName.isEmpty()
                        && !f.categoryName.equals(item.categoryKey)) continue;
                combined.add(new CombinedItem(item));
            }
        }

        if (api != null && !sourceIsCustom) {
            for (ItemEntity item : api) combined.add(new CombinedItem(item));
        }

        combined.sort((a, b) -> a.name.compareTo(b.name));
        result.setValue(combined);
    }

    public void loadSourcesAndCategories() {
        new Thread(() -> {
            List<String> dbSources = new ArrayList<>(itemDao.getDistinctSources());
            dbSources.add(0, "custom");
            sources.postValue(dbSources);

            List<String> dbCategories = new ArrayList<>(itemDao.getDistinctCategories());
            categories.postValue(dbCategories);
        }).start();
    }

    public void applyFilter(ItemFilter newFilter) { filter.setValue(newFilter); }

    public void setMagicOnly(boolean magicOnly) {
        ItemFilter current = filter.getValue();
        if (current == null) current = new ItemFilter();
        ItemFilter f = new ItemFilter();
        f.query = current.query;
        f.categoryName = current.categoryName;
        f.source = current.source;
        f.magicOnly = magicOnly;
        f.rarity = current.rarity;
        f.requiresAttunement = current.requiresAttunement;
        filter.setValue(f);
    }


    public void setRarity(String rarity) {
        ItemFilter current = filter.getValue();
        if (current == null) current = new ItemFilter();
        ItemFilter f = new ItemFilter();
        f.query = current.query;
        f.categoryName = current.categoryName;
        f.source = current.source;
        f.magicOnly = current.magicOnly;
        f.rarity = rarity != null ? rarity : "";
        f.requiresAttunement = current.requiresAttunement;
        filter.setValue(f);
    }

    public void setAttunement(boolean attunement) {
        ItemFilter current = filter.getValue();
        if (current == null) current = new ItemFilter();
        ItemFilter f = new ItemFilter();
        f.query = current.query;
        f.categoryName = current.categoryName;
        f.source = current.source;
        f.magicOnly = current.magicOnly;
        f.rarity = current.rarity;
        f.requiresAttunement = attunement;
        filter.setValue(f);
    }

    public void setQuery(String q) {
        ItemFilter current = filter.getValue();
        if (current == null) current = new ItemFilter();
        ItemFilter f = new ItemFilter();
        f.query = q != null ? q : "";
        f.categoryName = current.categoryName;
        f.source = current.source;
        f.magicOnly = current.magicOnly;
        f.rarity = current.rarity;
        f.requiresAttunement = current.requiresAttunement;
        filter.setValue(f);
    }

    public void setCategory(String categoryName) {
        ItemFilter current = filter.getValue();
        if (current == null) current = new ItemFilter();
        ItemFilter f = new ItemFilter();
        f.query = current.query;
        f.categoryName = categoryName != null ? categoryName : "";
        f.source = current.source;
        f.magicOnly = current.magicOnly;
        f.rarity = current.rarity;
        f.requiresAttunement = current.requiresAttunement;
        filter.setValue(f);
    }
    public void setSource(String source) {
        ItemFilter current = filter.getValue();
        if (current == null) current = new ItemFilter();
        ItemFilter f = new ItemFilter();
        f.query = current.query;
        f.categoryName = current.categoryName;
        f.source = source != null ? source : "";
        f.magicOnly = current.magicOnly;
        f.rarity = current.rarity;
        f.requiresAttunement = current.requiresAttunement;
        filter.setValue(f);
    }

    public void clearFilters() {
        ItemFilter f = new ItemFilter();
        f.query = "";
        f.categoryName = "";
        f.source = "";
        f.magicOnly = false;
        f.rarity = "";
        f.requiresAttunement = false;
        filter.setValue(f);
    }

    public LiveData<List<CombinedItem>> getItems() { return combinedItems; }
    public LiveData<ItemFilter> getFilter() { return filter; }
    public LiveData<List<String>> getSources() { return sources; }
    public LiveData<List<String>> getCategories() { return categories; }

    public static class Factory extends ViewModelProvider.NewInstanceFactory {
        private final ItemDao itemDao;
        private final CustomItemDao customItemDao;

        public Factory(ItemDao itemDao, CustomItemDao customItemDao) {
            this.itemDao = itemDao;
            this.customItemDao = customItemDao;
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            return (T) new ItemListViewModel(itemDao, customItemDao);
        }
    }
}
