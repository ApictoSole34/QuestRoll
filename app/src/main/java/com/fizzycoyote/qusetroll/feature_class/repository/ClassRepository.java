package com.fizzycoyote.qusetroll.feature_class.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.Transformations;

import com.fizzycoyote.qusetroll.core.models.custom.custom_character_class.CustomCharacterClassDao;
import com.fizzycoyote.qusetroll.core.models.custom.custom_character_class.CustomCharacterClassWithFeatures;
import com.fizzycoyote.qusetroll.core.models.open5e.character_class.CharacterClassDao;
import com.fizzycoyote.qusetroll.core.models.open5e.character_class.CharacterClassEntity;
import com.fizzycoyote.qusetroll.feature_class.model.CombinedClass;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

public class ClassRepository {
    private final CharacterClassDao open5eDao;
    private final CustomCharacterClassDao customDao;
    private final Executor executor;

    public ClassRepository(CharacterClassDao open5eDao,
                           CustomCharacterClassDao customDao,
                           Executor executor) {
        this.open5eDao = open5eDao;
        this.customDao = customDao;
        this.executor = executor;
    }

    public CustomCharacterClassDao getCustomDao() {
        return customDao;
    }

    public Executor getExecutor() {
        return executor;
    }

    public LiveData<List<CombinedClass>> getCombinedClasses() {
        MediatorLiveData<List<CombinedClass>> mediator = new MediatorLiveData<>();

        LiveData<List<CharacterClassEntity>> open5eLive = open5eDao.getAllClasses();
        LiveData<List<CustomCharacterClassWithFeatures>> customLive = customDao.getAllClasses();

        mediator.addSource(open5eLive, open5e ->
                combineData(open5e, customLive.getValue(), mediator));

        mediator.addSource(customLive, custom ->
                combineData(open5eLive.getValue(), custom, mediator));

        return mediator;
    }

    private void combineData(
            List<CharacterClassEntity> open5e,
            List<CustomCharacterClassWithFeatures> custom,
            MediatorLiveData<List<CombinedClass>> result
    ) {
        executor.execute(() -> {
            List<CombinedClass> combined = new ArrayList<>();
            Map<String, String> classMap = new HashMap<>();

            // Mapowanie nazw klas
            if (open5e != null) {
                for (CharacterClassEntity c : open5e) {
                    classMap.put(c.key, c.name);
                }
            }

            if (custom != null) {
                for (CustomCharacterClassWithFeatures c : custom) {
                    String key = "custom_" + c.characterClassEntity.id;
                    classMap.put(key, c.characterClassEntity.name);
                }
            }

            // Dodawanie klas Open5e
            if (open5e != null) {
                for (CharacterClassEntity c : open5e) {
                    combined.add(new CombinedClass(
                            c.key,
                            c.name,
                            false,
                            c.subclassOfKey,
                            classMap.getOrDefault(c.subclassOfKey, null)
                    ));
                }
            }

            // Dodawanie klas customowych
            if (custom != null) {
                for (CustomCharacterClassWithFeatures c : custom) {
                    String customKey = "custom_" + c.characterClassEntity.id;
                    String parentKey = c.characterClassEntity.subclassOf;

                    // Obsługa customowych rodziców
                    if (parentKey != null && parentKey.startsWith("custom_")) {
                        parentKey = parentKey.replace("custom_", "");
                    }

                    combined.add(new CombinedClass(
                            customKey,
                            c.characterClassEntity.name,
                            true,
                            parentKey,
                            classMap.getOrDefault(parentKey, null)
                    ));
                }
            }

            result.postValue(combined);
        });
    }
}