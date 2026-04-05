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
import com.fizzycoyote.qusetroll.core.models.custom.custom_item_category.CustomItemCategoryDao;
import com.fizzycoyote.qusetroll.core.models.custom.custom_item_category.CustomItemCategoryEntity;
import com.fizzycoyote.qusetroll.core.models.custom.custom_item_rarity.CustomItemRarityDao;
import com.fizzycoyote.qusetroll.core.models.custom.custom_item_rarity.CustomItemRarityEntity;
import com.fizzycoyote.qusetroll.core.models.custom.custom_weapon_property.CustomWeaponPropertyDao;
import com.fizzycoyote.qusetroll.core.models.custom.custom_weapon_property.CustomWeaponPropertyEntity;
import com.fizzycoyote.qusetroll.core.models.open5e.item.ItemDao;
import com.fizzycoyote.qusetroll.core.models.open5e.item.ItemDto;
import com.fizzycoyote.qusetroll.core.models.open5e.item.ItemEntity;
import com.fizzycoyote.qusetroll.core.models.open5e.item_category.ItemCategoryDao;
import com.fizzycoyote.qusetroll.core.models.open5e.item_category.ItemCategoryEntity;
import com.fizzycoyote.qusetroll.core.models.open5e.item_rarity.ItemRarityDao;
import com.fizzycoyote.qusetroll.core.models.open5e.item_rarity.ItemRarityEntity;
import com.fizzycoyote.qusetroll.core.models.open5e.weapon_property.WeaponPropertyDao;
import com.fizzycoyote.qusetroll.core.models.open5e.weapon_property.WeaponPropertyEntity;
import com.fizzycoyote.qusetroll.feature_item.model.CombinedItem;
import com.fizzycoyote.qusetroll.feature_item.model.ItemFilter;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

public class ItemListViewModel extends ViewModel {

    private final ItemDao itemDao;
    private final CustomItemDao customItemDao;
    private final ItemCategoryDao itemCategoryDao;
    private final CustomItemCategoryDao customItemCategoryDao;
    private final ItemRarityDao itemRarityDao;
    private final CustomItemRarityDao customItemRarityDao;
    private final WeaponPropertyDao weaponPropertyDao;
    private final CustomWeaponPropertyDao customWeaponPropertyDao;

    private final MutableLiveData<ItemFilter> filter = new MutableLiveData<>(new ItemFilter());
    private final LiveData<List<CombinedItem>> combinedItems;
    private final MutableLiveData<List<String>> sources = new MutableLiveData<>();
    private final LiveData<List<String>> categories;
    private MutableLiveData<List<String>> allCategoryNames = new MutableLiveData<>();
    private final LiveData<List<String>> combinedRarityNamesForFilter;
    private final LiveData<List<String>> combinedWeaponPropertyNamesForFilter;


    public ItemListViewModel(ItemDao itemDao, CustomItemDao customItemDao,
                             ItemCategoryDao itemCategoryDao,
                             CustomItemCategoryDao customItemCategoryDao,
                             ItemRarityDao itemRarityDao,
                             CustomItemRarityDao customItemRarityDao,
                             WeaponPropertyDao weaponPropertyDao,
                             CustomWeaponPropertyDao customWeaponPropertyDao) {
        this.itemDao = itemDao;
        this.customItemDao = customItemDao;
        this.itemCategoryDao = itemCategoryDao;
        this.customItemCategoryDao = customItemCategoryDao;
        this.itemRarityDao = itemRarityDao;
        this.customItemRarityDao = customItemRarityDao;
        this.weaponPropertyDao = weaponPropertyDao;
        this.customWeaponPropertyDao = customWeaponPropertyDao;

        LiveData<List<ItemEntity>> apiItems = Transformations.switchMap(filter, f ->
                itemDao.getFiltered(f.query, f.categoryName, f.source, f.magicOnly ? 1 : 0, f.rarity, f.requiresAttunement ? 1 : 0)
        );
        LiveData<List<CustomItemEntity>> customItems = customItemDao.getAll();

        MediatorLiveData<List<CombinedItem>> mediator = new MediatorLiveData<>();
        mediator.addSource(apiItems, api -> combine(api, customItems.getValue(), mediator));
        mediator.addSource(customItems, custom -> combine(apiItems.getValue(), custom, mediator));
        combinedItems = mediator;

        LiveData<List<ItemCategoryEntity>> apiCatsLive = itemCategoryDao.getAll();
        LiveData<List<CustomItemCategoryEntity>> customCatsLive = customItemCategoryDao.getAll();

        MediatorLiveData<List<String>> catMediator = new MediatorLiveData<>();
        Runnable updateCategories = () -> {
            List<String> names = new ArrayList<>();
            names.add("All");

            List<ItemCategoryEntity> apiCats = apiCatsLive.getValue();
            if (apiCats != null) {
                for (ItemCategoryEntity c : apiCats) names.add(c.name);
            }
            List<CustomItemCategoryEntity> customCats = customCatsLive.getValue();
            if (customCats != null) {
                for (CustomItemCategoryEntity c : customCats) {
                    if (!names.contains(c.name)) names.add(c.name);
                }
            }
            catMediator.setValue(names);
        };
        apiCatsLive.observeForever(ignored -> updateCategories.run());
        customCatsLive.observeForever(ignored -> updateCategories.run());
        updateCategories.run();
        allCategoryNames = catMediator;

        categories = Transformations.map(allCategoryNames, names -> {
            List<String> filtered = new ArrayList<>(names);
            filtered.remove("All");
            return filtered;
        });


        LiveData<List<ItemRarityEntity>> apiRaritiesLive = itemRarityDao.getAll();
        LiveData<List<CustomItemRarityEntity>> customRaritiesLive = customItemRarityDao.getAll();
        MediatorLiveData<List<String>> rarityMediator = new MediatorLiveData<>();
        Runnable updateRarities = () -> {
            List<String> names = new ArrayList<>();
            names.add("Any");
            List<ItemRarityEntity> api = apiRaritiesLive.getValue();
            if (api != null) {
                for (ItemRarityEntity r : api) names.add(r.name);
            }
            List<CustomItemRarityEntity> custom = customRaritiesLive.getValue();
            if (custom != null) {
                for (CustomItemRarityEntity r : custom) {
                    if (!names.contains(r.name)) names.add(r.name);
                }
            }
            rarityMediator.setValue(names);
        };
        apiRaritiesLive.observeForever(ignored -> updateRarities.run());
        customRaritiesLive.observeForever(ignored -> updateRarities.run());
        updateRarities.run();
        combinedRarityNamesForFilter = rarityMediator;

        LiveData<List<WeaponPropertyEntity>> apiPropsLive = weaponPropertyDao.getAll();
        LiveData<List<CustomWeaponPropertyEntity>> customPropsLive = customWeaponPropertyDao.getAll();
        MediatorLiveData<List<String>> propMediator = new MediatorLiveData<>();
        Runnable updateProps = () -> {
            List<String> names = new ArrayList<>();
            names.add("Any");
            List<WeaponPropertyEntity> api = apiPropsLive.getValue();
            if (api != null) {
                for (WeaponPropertyEntity p : api) names.add(p.name);
            }
            List<CustomWeaponPropertyEntity> custom = customPropsLive.getValue();
            if (custom != null) {
                for (CustomWeaponPropertyEntity p : custom) {
                    if (!names.contains(p.name)) names.add(p.name);
                }
            }
            propMediator.setValue(names);
        };
        apiPropsLive.observeForever(ignored -> updateProps.run());
        customPropsLive.observeForever(ignored -> updateProps.run());
        updateProps.run();
        combinedWeaponPropertyNamesForFilter = propMediator;
    }

    private void combine(List<ItemEntity> api, List<CustomItemEntity> custom,
                         MediatorLiveData<List<CombinedItem>> result) {
        List<CombinedItem> combined = new ArrayList<>();
        ItemFilter f = filter.getValue();
        boolean sourceIsCustom = f != null && "custom".equals(f.source);

        java.util.function.BiFunction<String, String, Boolean> hasProperty = (weaponJson, propertyName) -> {
            if (weaponJson == null || weaponJson.isEmpty() || propertyName == null || propertyName.isEmpty())
                return false;
            try {
                ItemDto.WeaponEmbedDto weapon = new Gson().fromJson(weaponJson, ItemDto.WeaponEmbedDto.class);
                if (weapon.properties != null) {
                    for (ItemDto.WeaponEmbedDto.WeaponPropertyDto wp : weapon.properties) {
                        if (wp.property != null && wp.property.name != null && wp.property.name.equalsIgnoreCase(propertyName)) {
                            return true;
                        }
                    }
                }
            } catch (Exception e) {}
            return false;
        };

        if (custom != null) {
            boolean showCustom = sourceIsCustom || (f != null && f.source.isEmpty());
            if (showCustom) {
                for (CustomItemEntity item : custom) {
                    if (f != null && !f.query.isEmpty()
                            && !item.name.toLowerCase().contains(f.query.toLowerCase())) continue;
                    if (f != null && !f.categoryName.isEmpty()
                            && !f.categoryName.equals(item.categoryName)) continue;
                    if (f != null && f.magicOnly && !item.isMagicItem) continue;
                    if (f != null && !f.rarity.isEmpty()
                            && !f.rarity.equals(item.rarityName)) continue;
                    if (f != null && f.requiresAttunement && !item.requiresAttunement) continue;
                    if (f != null && !f.weaponProperty.isEmpty()) {
                        if (!"Weapon".equalsIgnoreCase(item.categoryName)) continue;
                        if (!hasProperty.apply(item.weaponJson, f.weaponProperty)) continue;
                    }
                    combined.add(new CombinedItem(item));
                }
            }
        }

        if (api != null && !sourceIsCustom) {
            for (ItemEntity item : api) {
                if (f != null && !f.query.isEmpty()
                        && !item.name.toLowerCase().contains(f.query.toLowerCase())) continue;
                if (f != null && !f.categoryName.isEmpty()
                        && !f.categoryName.equals(item.categoryName)) continue;
                if (f != null && f.magicOnly && !item.isMagicItem) continue;
                if (f != null && !f.rarity.isEmpty()
                        && !f.rarity.equals(item.rarityName)) continue;
                if (f != null && f.requiresAttunement && !item.requiresAttunement) continue;
                if (f != null && !f.weaponProperty.isEmpty()) {
                    if (!"Weapon".equalsIgnoreCase(item.categoryName)) continue;
                    if (!hasProperty.apply(item.weaponJson, f.weaponProperty)) continue;
                }
                combined.add(new CombinedItem(item));
            }
        }

        combined.sort((a, b) -> a.name.compareTo(b.name));
        result.setValue(combined);
    }


    public void loadSourcesAndCategories() {
        new Thread(() -> {
            List<String> dbSources = new ArrayList<>(itemDao.getDistinctSources());
            dbSources.add(0, "custom");
            sources.postValue(dbSources);
        }).start();
    }

    public void applyFilter(ItemFilter newFilter) { filter.setValue(newFilter); }
    public void setQuery(String q) {
        ItemFilter f = new ItemFilter();
        ItemFilter old = filter.getValue();
        if (old != null) { f.categoryName = old.categoryName; f.source = old.source; f.magicOnly = old.magicOnly; f.rarity = old.rarity; f.requiresAttunement = old.requiresAttunement; }
        f.query = q != null ? q : "";
        filter.setValue(f);
    }
    public void setCategory(String categoryName) {
        ItemFilter f = new ItemFilter();
        ItemFilter old = filter.getValue();
        if (old != null) { f.query = old.query; f.source = old.source; f.magicOnly = old.magicOnly; f.rarity = old.rarity; f.requiresAttunement = old.requiresAttunement; }
        f.categoryName = categoryName != null ? categoryName : "";
        filter.setValue(f);
    }
    public void setSource(String source) {
        ItemFilter f = new ItemFilter();
        ItemFilter old = filter.getValue();
        if (old != null) { f.query = old.query; f.categoryName = old.categoryName; f.magicOnly = old.magicOnly; f.rarity = old.rarity; f.requiresAttunement = old.requiresAttunement; }
        f.source = source != null ? source : "";
        filter.setValue(f);
    }
    public void clearFilters() { filter.setValue(new ItemFilter()); }

    public LiveData<List<CombinedItem>> getItems() { return combinedItems; }
    public LiveData<ItemFilter> getFilter() { return filter; }
    public LiveData<List<String>> getSources() { return sources; }
    public LiveData<List<String>> getCategories() { return categories; }
    public LiveData<List<String>> getAllCategoryNames() { return allCategoryNames; }
    public LiveData<List<String>> getCombinedRarityNamesForFilter() { return combinedRarityNamesForFilter; }
    public LiveData<List<String>> getCombinedWeaponPropertyNamesForFilter() { return combinedWeaponPropertyNamesForFilter; }

    public static class Factory extends ViewModelProvider.NewInstanceFactory {
        private final ItemDao itemDao;
        private final CustomItemDao customItemDao;
        private final ItemCategoryDao itemCategoryDao;
        private final CustomItemCategoryDao customItemCategoryDao;
        private final ItemRarityDao itemRarityDao;
        private final CustomItemRarityDao customItemRarityDao;
        private final WeaponPropertyDao weaponPropertyDao;
        private final CustomWeaponPropertyDao customWeaponPropertyDao;

        public Factory(ItemDao itemDao, CustomItemDao customItemDao,
                       ItemCategoryDao itemCategoryDao,
                       CustomItemCategoryDao customItemCategoryDao,
                       ItemRarityDao itemRarityDao,
                       CustomItemRarityDao customItemRarityDao, WeaponPropertyDao weaponPropertyDao,
                       CustomWeaponPropertyDao customWeaponPropertyDao) {
            this.itemDao = itemDao;
            this.customItemDao = customItemDao;
            this.itemCategoryDao = itemCategoryDao;
            this.customItemCategoryDao = customItemCategoryDao;
            this.itemRarityDao = itemRarityDao;
            this.customItemRarityDao = customItemRarityDao;
            this.weaponPropertyDao = weaponPropertyDao;
            this.customWeaponPropertyDao = customWeaponPropertyDao;

        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            return (T) new ItemListViewModel(itemDao, customItemDao, itemCategoryDao,
                    customItemCategoryDao, itemRarityDao, customItemRarityDao, weaponPropertyDao,
                    customWeaponPropertyDao);
        }
    }
}