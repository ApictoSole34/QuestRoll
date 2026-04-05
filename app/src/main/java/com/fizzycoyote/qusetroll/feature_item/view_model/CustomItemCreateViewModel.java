package com.fizzycoyote.qusetroll.feature_item.view_model;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.fizzycoyote.qusetroll.core.models.custom.custom_creature.CustomCreatureAction;
import com.fizzycoyote.qusetroll.core.models.custom.custom_damage_types.CustomDamageTypeDao;
import com.fizzycoyote.qusetroll.core.models.custom.custom_damage_types.CustomDamageTypeEntity;
import com.fizzycoyote.qusetroll.core.models.custom.custom_item.CustomItemDao;
import com.fizzycoyote.qusetroll.core.models.custom.custom_item.CustomItemEntity;
import com.fizzycoyote.qusetroll.core.models.custom.custom_item_category.CustomItemCategoryDao;
import com.fizzycoyote.qusetroll.core.models.custom.custom_item_category.CustomItemCategoryEntity;
import com.fizzycoyote.qusetroll.core.models.custom.custom_item_rarity.CustomItemRarityDao;
import com.fizzycoyote.qusetroll.core.models.custom.custom_item_rarity.CustomItemRarityEntity;
import com.fizzycoyote.qusetroll.core.models.custom.custom_weapon_property.CustomWeaponPropertyDao;
import com.fizzycoyote.qusetroll.core.models.custom.custom_weapon_property.CustomWeaponPropertyEntity;
import com.fizzycoyote.qusetroll.core.models.open5e.damage_type.DamageTypeDao;
import com.fizzycoyote.qusetroll.core.models.open5e.damage_type.DamageTypeEntity;
import com.fizzycoyote.qusetroll.core.models.open5e.item.ItemDto;
import com.fizzycoyote.qusetroll.core.models.open5e.item_category.ItemCategoryDao;
import com.fizzycoyote.qusetroll.core.models.open5e.item_category.ItemCategoryEntity;
import com.fizzycoyote.qusetroll.core.models.open5e.item_rarity.ItemRarityDao;
import com.fizzycoyote.qusetroll.core.models.open5e.item_rarity.ItemRarityEntity;
import com.fizzycoyote.qusetroll.core.models.open5e.weapon_property.WeaponPropertyDao;
import com.fizzycoyote.qusetroll.core.models.open5e.weapon_property.WeaponPropertyEntity;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.Executor;

public class CustomItemCreateViewModel extends ViewModel {

    private final CustomItemDao dao;
    private final long editId;
    private final Executor executor;
    private final ItemCategoryDao itemCategoryDao;
    private final CustomItemCategoryDao customItemCategoryDao;
    private final ItemRarityDao itemRarityDao;
    private final DamageTypeDao damageTypeDao;
    private final CustomItemRarityDao customRarityDao;
    private final CustomDamageTypeDao customDamageTypeDao;
    private final LiveData<List<String>> combinedRarityNames;
    private final LiveData<List<String>> combinedDamageTypeNames;
    private final LiveData<List<String>> allCategoryNames;
    private final WeaponPropertyDao apiWeaponPropertyDao;
    private final CustomWeaponPropertyDao customWeaponPropertyDao;
    private final LiveData<List<String>> allWeaponPropertyNames;

    private final MutableLiveData<Set<String>> selectedWeaponProperties = new MutableLiveData<>(new LinkedHashSet<>());
    private final MutableLiveData<CustomItemEntity> editData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> saveResult = new MutableLiveData<>();
    private final MutableLiveData<List<CustomCreatureAction>> properties = new MutableLiveData<>(new ArrayList<>());

    public CustomItemCreateViewModel(CustomItemDao dao, long editId, Executor executor,
                                     ItemCategoryDao itemCategoryDao,
                                     CustomItemCategoryDao customItemCategoryDao,
                                     ItemRarityDao itemRarityDao,
                                     CustomItemRarityDao customRarityDao,
                                     DamageTypeDao damageTypeDao,
                                     CustomDamageTypeDao customDamageTypeDao, WeaponPropertyDao apiWeaponPropertyDao,
                                     CustomWeaponPropertyDao customWeaponPropertyDao) {
        this.dao = dao;
        this.editId = editId;
        this.executor = executor;
        this.itemCategoryDao = itemCategoryDao;
        this.customItemCategoryDao = customItemCategoryDao;
        this.itemRarityDao = itemRarityDao;
        this.customRarityDao = customRarityDao;
        this.damageTypeDao = damageTypeDao;
        this.customDamageTypeDao = customDamageTypeDao;
        this.apiWeaponPropertyDao = apiWeaponPropertyDao;
        this.customWeaponPropertyDao = customWeaponPropertyDao;

        LiveData<List<ItemCategoryEntity>> apiCatsLive = itemCategoryDao.getAll();
        LiveData<List<CustomItemCategoryEntity>> customCatsLive = customItemCategoryDao.getAll();

        LiveData<List<WeaponPropertyEntity>> apiProps = apiWeaponPropertyDao.getAll();
        LiveData<List<CustomWeaponPropertyEntity>> customProps = customWeaponPropertyDao.getAll();
        MediatorLiveData<List<String>> namesMediator = new MediatorLiveData<>();
        Runnable updateNames = () -> {
            Set<String> names = new TreeSet<>();
            List<WeaponPropertyEntity> api = apiProps.getValue();
            if (api != null) for (WeaponPropertyEntity p : api) names.add(p.name);
            List<CustomWeaponPropertyEntity> custom = customProps.getValue();
            if (custom != null) for (CustomWeaponPropertyEntity c : custom) names.add(c.name);
            namesMediator.setValue(new ArrayList<>(names));
        };
        apiProps.observeForever(ignored -> updateNames.run());
        customProps.observeForever(ignored -> updateNames.run());
        updateNames.run();
        allWeaponPropertyNames = namesMediator;

        MediatorLiveData<List<String>> catMediator = new MediatorLiveData<>();
        Runnable update = () -> {
            List<String> names = new ArrayList<>();
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

        LiveData<List<ItemRarityEntity>> apiRaritiesLive = itemRarityDao.getAll();
        LiveData<List<CustomItemRarityEntity>> customRaritiesLive = customRarityDao.getAll();
        MediatorLiveData<List<String>> rarityMediator = new MediatorLiveData<>();
        Runnable updateRarities = () -> {
            List<String> names = new ArrayList<>();
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
            Collections.sort(names);
            rarityMediator.setValue(names);
        };

        apiRaritiesLive.observeForever(ignored -> updateRarities.run());
        customRaritiesLive.observeForever(ignored -> updateRarities.run());
        updateRarities.run();
        combinedRarityNames = rarityMediator;

        LiveData<List<DamageTypeEntity>> apiDamageLive = damageTypeDao.getAll();
        LiveData<List<CustomDamageTypeEntity>> customDamageLive = customDamageTypeDao.getAll();
        MediatorLiveData<List<String>> damageMediator = new MediatorLiveData<>();
        Runnable updateDamage = () -> {
            List<String> names = new ArrayList<>();
            List<DamageTypeEntity> api = apiDamageLive.getValue();
            if (api != null) {
                for (DamageTypeEntity dt : api) names.add(dt.name);
            }
            List<CustomDamageTypeEntity> custom = customDamageLive.getValue();
            if (custom != null) {
                for (CustomDamageTypeEntity dt : custom) {
                    if (!names.contains(dt.name)) names.add(dt.name);
                }
            }
            Collections.sort(names);
            damageMediator.setValue(names);
        };
        apiDamageLive.observeForever(ignored -> updateDamage.run());
        customDamageLive.observeForever(ignored -> updateDamage.run());
        updateDamage.run();
        combinedDamageTypeNames = damageMediator;

        apiCatsLive.observeForever(ignored -> update.run());
        customCatsLive.observeForever(ignored -> update.run());
        update.run();
        allCategoryNames = catMediator;

        if (isEditMode()) loadExisting();
    }

    public boolean isEditMode() { return editId != -1; }

    private void loadExisting() {
        dao.getById(editId).observeForever(item -> {
            if (item != null && editData.getValue() == null) {
                editData.setValue(item);
                if (item.categoryName != null && item.categoryName.equalsIgnoreCase("Weapon") && item.weaponJson != null) {
                    try {
                        ItemDto.WeaponEmbedDto weapon = new Gson().fromJson(item.weaponJson, ItemDto.WeaponEmbedDto.class);
                        if (weapon.properties != null && !weapon.properties.isEmpty()) {
                            Set<String> props = new LinkedHashSet<>();
                            for (ItemDto.WeaponEmbedDto.WeaponPropertyDto wp : weapon.properties) {
                                if (wp.property != null && wp.property.name != null) {
                                    props.add(wp.property.name);
                                }
                            }
                            selectedWeaponProperties.setValue(props);
                        } else {
                            selectedWeaponProperties.setValue(new LinkedHashSet<>());
                        }
                    } catch (Exception e) {
                        selectedWeaponProperties.setValue(new LinkedHashSet<>());
                    }
                } else {
                    selectedWeaponProperties.setValue(new LinkedHashSet<>());
                }
                properties.setValue(new ArrayList<>());
            }
        });
    }

    public LiveData<CustomItemEntity> getEditData() { return editData; }
    public LiveData<Boolean> getSaveResult() { return saveResult; }
    public LiveData<List<CustomCreatureAction>> getProperties() { return properties; }
    public LiveData<List<String>> getAllCategoryNames() { return allCategoryNames; }
    public LiveData<List<String>> getCombinedRarityNames() { return combinedRarityNames; }
    public LiveData<List<String>> getCombinedDamageTypeNames() { return combinedDamageTypeNames; }
    public LiveData<List<String>> getAllWeaponPropertyNames() { return allWeaponPropertyNames; }
    public LiveData<Set<String>> getSelectedWeaponProperties() { return selectedWeaponProperties; }

    public void addWeaponProperty(String propertyName) {
        Set<String> current = new LinkedHashSet<>(selectedWeaponProperties.getValue());
        if (current.add(propertyName)) {
            selectedWeaponProperties.setValue(current);
        }
    }

    public void removeWeaponProperty(String propertyName) {
        Set<String> current = new LinkedHashSet<>(selectedWeaponProperties.getValue());
        if (current.remove(propertyName)) {
            selectedWeaponProperties.setValue(current);
        }
    }

    public void setProperties(List<CustomCreatureAction> props) {
        properties.setValue(props);
    }

    public void save(CustomItemEntity item) {
        executor.execute(() -> {
            try {
                if (item.categoryName != null && item.categoryName.equalsIgnoreCase("Weapon")) {
                    ItemDto.WeaponEmbedDto weapon;
                    try {
                        weapon = new Gson().fromJson(item.weaponJson, ItemDto.WeaponEmbedDto.class);
                        if (weapon == null) weapon = new ItemDto.WeaponEmbedDto();
                    } catch (Exception e) {
                        weapon = new ItemDto.WeaponEmbedDto();
                    }
                    ItemDto.WeaponEmbedDto newWeapon = new ItemDto.WeaponEmbedDto();
                    newWeapon.damageDice = item.weaponJson != null ?
                            new Gson().fromJson(item.weaponJson, ItemDto.WeaponEmbedDto.class).damageDice : "";
                    ItemDto.WeaponEmbedDto existingWeapon;
                    try {
                        existingWeapon = new Gson().fromJson(item.weaponJson, ItemDto.WeaponEmbedDto.class);
                    } catch (Exception e) {
                        existingWeapon = new ItemDto.WeaponEmbedDto();
                    }
                    List<ItemDto.WeaponEmbedDto.WeaponPropertyDto> propList = new ArrayList<>();
                    Set<String> selectedProps = selectedWeaponProperties.getValue();
                    if (selectedProps != null) {
                        for (String propName : selectedProps) {
                            ItemDto.WeaponEmbedDto.WeaponPropertyDto wp = new ItemDto.WeaponEmbedDto.WeaponPropertyDto();
                            wp.detail = "";
                            ItemDto.WeaponEmbedDto.WeaponPropertyDto.PropertyDetailDto detail = new ItemDto.WeaponEmbedDto.WeaponPropertyDto.PropertyDetailDto();
                            detail.name = propName;
                            wp.property = detail;
                            propList.add(wp);
                        }
                    }
                    existingWeapon.properties = propList;
                    item.weaponJson = new Gson().toJson(existingWeapon);
                }
                if (isEditMode()) {
                    item.id = editId;
                    dao.update(item);
                } else {
                    if (dao.countByName(item.name) > 0) {
                        saveResult.postValue(false);
                        return;
                    }
                    dao.insert(item);
                }
                saveResult.postValue(true);
            } catch (Exception e) {
                saveResult.postValue(false);
            }
        });
    }

    public static class Factory extends ViewModelProvider.NewInstanceFactory {
        private final CustomItemDao dao;
        private final long editId;
        private final Executor executor;
        private final ItemCategoryDao itemCategoryDao;
        private final CustomItemCategoryDao customItemCategoryDao;
        private final ItemRarityDao itemRarityDao;
        private final CustomItemRarityDao customRarityDao;
        private final DamageTypeDao damageTypeDao;
        private final CustomDamageTypeDao customDamageTypeDao;
        private final WeaponPropertyDao apiWeaponPropertyDao;
        private final CustomWeaponPropertyDao customWeaponPropertyDao;


        public Factory(CustomItemDao dao, long editId, Executor executor,
                       ItemCategoryDao itemCategoryDao, CustomItemCategoryDao customItemCategoryDao,
                       ItemRarityDao itemRarityDao, CustomItemRarityDao customRarityDao,
                       DamageTypeDao damageTypeDao, CustomDamageTypeDao customDamageTypeDao,
                       WeaponPropertyDao apiWeaponPropertyDao, CustomWeaponPropertyDao customWeaponPropertyDao) {
            this.dao = dao;
            this.editId = editId;
            this.executor = executor;
            this.itemCategoryDao = itemCategoryDao;
            this.customItemCategoryDao = customItemCategoryDao;
            this.itemRarityDao = itemRarityDao;
            this.customRarityDao = customRarityDao;
            this.damageTypeDao = damageTypeDao;
            this.customDamageTypeDao = customDamageTypeDao;
            this.apiWeaponPropertyDao = apiWeaponPropertyDao;
            this.customWeaponPropertyDao = customWeaponPropertyDao;
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            return (T) new CustomItemCreateViewModel(dao, editId, executor,
                    itemCategoryDao, customItemCategoryDao,
                    itemRarityDao, customRarityDao,
                    damageTypeDao, customDamageTypeDao, apiWeaponPropertyDao, customWeaponPropertyDao);
        }
    }
}