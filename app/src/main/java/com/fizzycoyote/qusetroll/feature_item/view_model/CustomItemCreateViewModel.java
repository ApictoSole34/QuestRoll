package com.fizzycoyote.qusetroll.feature_item.view_model;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.fizzycoyote.qusetroll.core.models.custom.custom_creature.CustomCreatureAction;
import com.fizzycoyote.qusetroll.core.models.custom.custom_item.CustomItemDao;
import com.fizzycoyote.qusetroll.core.models.custom.custom_item.CustomItemEntity;
import com.fizzycoyote.qusetroll.core.models.open5e.item.ItemDto;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

public class CustomItemCreateViewModel extends ViewModel {
    private final CustomItemDao dao;
    private final long editId;
    private final Executor executor;

    private final MutableLiveData<CustomItemEntity> editData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> saveResult = new MutableLiveData<>();
    private final MutableLiveData<List<CustomCreatureAction>> properties = new MutableLiveData<>(new ArrayList<>());

    public CustomItemCreateViewModel(CustomItemDao dao, long editId, Executor executor) {
        this.dao = dao;
        this.editId = editId;
        this.executor = executor;
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
                            List<CustomCreatureAction> props = new ArrayList<>();
                            for (ItemDto.WeaponEmbedDto.WeaponPropertyDto wp : weapon.properties) {
                                CustomCreatureAction action = new CustomCreatureAction();
                                action.name = wp.property != null ? wp.property.name : "";
                                action.desc = wp.detail != null ? wp.detail : "";
                                props.add(action);
                            }
                            properties.setValue(props);
                        } else {
                            properties.setValue(new ArrayList<>());
                        }
                    } catch (Exception e) {}
                } else {
                    properties.setValue(new ArrayList<>());
                }
            }
        });
    }

    public LiveData<CustomItemEntity> getEditData() { return editData; }
    public LiveData<Boolean> getSaveResult() { return saveResult; }
    public LiveData<List<CustomCreatureAction>> getProperties() { return properties; }

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
                    List<ItemDto.WeaponEmbedDto.WeaponPropertyDto> propList = new ArrayList<>();
                    List<CustomCreatureAction> props = properties.getValue();
                    if (props != null) {
                        for (CustomCreatureAction action : props) {
                            ItemDto.WeaponEmbedDto.WeaponPropertyDto wp = new ItemDto.WeaponEmbedDto.WeaponPropertyDto();
                            wp.detail = action.desc;
                            ItemDto.WeaponEmbedDto.WeaponPropertyDto.PropertyDetailDto detail = new ItemDto.WeaponEmbedDto.WeaponPropertyDto.PropertyDetailDto();
                            detail.name = action.name;
                            wp.property = detail;
                            propList.add(wp);
                        }
                    }
                    weapon.properties = propList;
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

        public Factory(CustomItemDao dao, long editId, Executor executor) {
            this.dao = dao; this.editId = editId; this.executor = executor;
        }

        @NonNull @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            return (T) new CustomItemCreateViewModel(dao, editId, executor);
        }
    }
}