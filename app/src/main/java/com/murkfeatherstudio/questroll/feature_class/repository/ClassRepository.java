package com.murkfeatherstudio.questroll.feature_class.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import com.murkfeatherstudio.questroll.core.models.custom.custom_character_class.CustomCharacterClassDao;
import com.murkfeatherstudio.questroll.core.models.custom.custom_character_class.CustomCharacterClassWithFeatures;
import com.murkfeatherstudio.questroll.core.models.open5e.character_class.CharacterClassDao;
import com.murkfeatherstudio.questroll.core.models.open5e.character_class.CharacterClassEntity;
import com.murkfeatherstudio.questroll.feature_class.model.CombinedClass;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

/**
 * Repository for managing character classes, combining data from the official Open5e compendium
 * and user-created custom classes.
 */
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

    /**
     * Retrieves a merged list of all classes (official and custom).
     * <p>
     * Uses a {@link MediatorLiveData} to observe changes in both the official compendium
     * and custom character classes, updating the combined list whenever either source changes.
     * </p>
     *
     * @return LiveData containing the combined list of {@link CombinedClass}.
     */
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

    /**
     * Logic for merging official and custom class data into a unified list.
     * Maps subclass relationships and identifies official vs custom content.
     */
    private void combineData(
            List<CharacterClassEntity> open5e,
            List<CustomCharacterClassWithFeatures> custom,
            MediatorLiveData<List<CombinedClass>> result
    ) {
        executor.execute(() -> {
            List<CombinedClass> combined = new ArrayList<>();
            Map<String, String> classMap = new HashMap<>();

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

            if (open5e != null) {
                for (CharacterClassEntity c : open5e) {
                    combined.add(new CombinedClass(
                            c.key,
                            c.name,
                            false,
                            c.subclassOfKey,
                            classMap.getOrDefault(c.subclassOfKey, null),
                            "5e-2014"
                    ));
                }
            }

            if (custom != null) {
                for (CustomCharacterClassWithFeatures c : custom) {
                    String customKey = "custom_" + c.characterClassEntity.id;
                    String parentKey = c.characterClassEntity.subclassOf;
                    combined.add(new CombinedClass(
                            customKey,
                            c.characterClassEntity.name,
                            true,
                            parentKey,
                            classMap.getOrDefault(parentKey, null),
                            c.characterClassEntity.gameSystem
                    ));
                }
            }

            result.postValue(combined);
        });
    }
}
