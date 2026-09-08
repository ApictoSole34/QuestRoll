package com.murkfeatherstudio.questroll.feature_class.view_model;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.murkfeatherstudio.questroll.core.AppExecutors;
import com.murkfeatherstudio.questroll.core.local_database.UserContentDatabase;
import com.murkfeatherstudio.questroll.core.models.custom.custom_character_class.CustomCharacterClassEntity;
import com.murkfeatherstudio.questroll.core.models.custom.custom_character_class.CustomCharacterClassWithFeatures;
import com.murkfeatherstudio.questroll.core.models.custom.custom_character_class.custom_feature.CustomFeatureEntity;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * ViewModel for the Class Creation/Edition Wizard.
 */
public class ClassWizardViewModel extends AndroidViewModel {

    public ClassWizardViewModel(@NonNull Application application) {
        super(application);
    }

    // --- BASIC INFO ---
    public String className = "";
    public String hitDice = "d8";
    public String description = "";
    public String gameSystem = "5e-2014";
    public String casterType = "NONE";
    public String spellcastingAbility = "NONE";

    // --- MULTICLASS PREREQUISITES ---
    public Map<String, Integer> multiclassPrereqs = new HashMap<>();

    // --- PROFICIENCIES ---
    public Set<String> savingThrows = new HashSet<>();
    public int skillChoicesCount = 2;
    public List<String> skillOptions = new ArrayList<>();

    // --- PROGRESSION ---
    public List<ClassProgressionRow> progression = new ArrayList<>();

    // --- SUBCLASS ---
    public boolean isSubclass = false;
    public String parentClassKey = null;
    public String parentClassName = null;

    // --- FEATURES ---
    public List<CustomFeatureEntity> features = new ArrayList<>();

    // --- SPELLS ---
    public List<String> spellKeys = new ArrayList<>();

    // --- EQUIPMENT ---
    public String equipmentDescription = "";
    public String startingGoldDice = "5d4";
    public List<String> startingItems = new ArrayList<>();

    // --- LANGUAGES ---
    public List<String> fixedLanguages = new ArrayList<>();
    public int languageChoices = 0;

    // --- EDIT MODE ---
    private long editingClassId = -1;
    private boolean isEditMode = false;

    public final MutableLiveData<Boolean> editDataReady = new MutableLiveData<>(false);

    public boolean isEditMode() {
        return isEditMode;
    }

    public void setSubclassMode(boolean isSubclass) {
        this.isSubclass = isSubclass;
    }

    public void loadClassForEdit(long classId) {
        this.editingClassId = classId;
        this.isEditMode = true;

        AppExecutors.getInstance().diskIO().execute(() -> {
            UserContentDatabase db = UserContentDatabase.getInstance(getApplication());

            CustomCharacterClassWithFeatures data = db.customCharacterClassDao()
                    .getClassWithFeaturesSync(classId);

            if (data != null && data.characterClassEntity != null) {
                CustomCharacterClassEntity entity = data.characterClassEntity;
                List<CustomFeatureEntity> loadedFeatures = data.features;

                Gson gson = new Gson();
                Type stringListType = new TypeToken<List<String>>(){}.getType();
                Type progressionListType = new TypeToken<List<ClassProgressionRow>>(){}.getType();
                Type mapType = new TypeToken<Map<String, Integer>>(){}.getType();

                className = entity.name;
                hitDice = entity.hitDice != null ? entity.hitDice : "d8";
                description = entity.description != null ? entity.description : "";
                gameSystem = entity.gameSystem != null ? entity.gameSystem : "5e-2014";
                casterType = entity.casterType != null ? entity.casterType : "NONE";
                spellcastingAbility = entity.spellcastingAbility != null
                        ? entity.spellcastingAbility : "NONE";

                if (entity.multiclassPrereqsJson != null && !entity.multiclassPrereqsJson.isEmpty()) {
                    multiclassPrereqs = gson.fromJson(entity.multiclassPrereqsJson, mapType);
                } else {
                    multiclassPrereqs = new HashMap<>();
                }

                savingThrows = entity.savingThrows != null
                        ? new HashSet<>(entity.savingThrows) : new HashSet<>();
                skillChoicesCount = entity.skillChoicesCount;
                skillOptions = parseJsonList(gson, entity.skillOptionsJson, stringListType);

                isSubclass = entity.subclassOf != null && !entity.subclassOf.isEmpty();
                parentClassKey = entity.subclassOf;

                equipmentDescription = entity.equipmentDescription != null
                        ? entity.equipmentDescription : "";
                startingGoldDice = entity.startingGoldDice != null
                        ? entity.startingGoldDice : "5d4";
                startingItems = parseJsonList(gson, entity.startingItemsJson, stringListType);

                fixedLanguages = parseJsonList(gson, entity.languageKeysJson, stringListType);
                languageChoices = entity.languageChoices;

                spellKeys = parseJsonList(gson, entity.spellKeysJson, stringListType);

                if (entity.progressionJson != null && !entity.progressionJson.isEmpty()) {
                    List<ClassProgressionRow> loaded = gson.fromJson(
                            entity.progressionJson, progressionListType);
                    if (loaded != null) progression = loaded;
                }

                if (loadedFeatures != null) {
                    features = new ArrayList<>(loadedFeatures);
                }
            }

            editDataReady.postValue(true);
        });
    }

    private List<String> parseJsonList(Gson gson, String json, Type type) {
        if (json == null || json.isEmpty()) return new ArrayList<>();
        List<String> result = gson.fromJson(json, type);
        return result != null ? result : new ArrayList<>();
    }

    public void saveClass(Callback callback) {
        AppExecutors.getInstance().diskIO().execute(() -> {
            try {
                CustomCharacterClassEntity entity = buildEntity();

                UserContentDatabase db = UserContentDatabase.getInstance(getApplication());
                long classId;

                if (isEditMode) {
                    entity.id = editingClassId;
                    db.customCharacterClassDao().updateClass(entity);
                    classId = editingClassId;

                    db.customFeatureDao().deleteFeaturesForClass(classId);
                } else {
                    classId = db.customCharacterClassDao().insertClass(entity);
                }

                for (CustomFeatureEntity feature : features) {
                    feature.classId = classId;
                }
                if (!features.isEmpty()) {
                    db.customFeatureDao().insertAll(features);
                }

                AppExecutors.getInstance().mainThread().execute(() -> callback.onResult(true));

            } catch (Exception e) {
                e.printStackTrace();
                AppExecutors.getInstance().mainThread().execute(() -> callback.onResult(false));
            }
        });
    }

    private CustomCharacterClassEntity buildEntity() {
        CustomCharacterClassEntity entity = new CustomCharacterClassEntity();
        Gson gson = new Gson();

        entity.name = className;
        entity.hitDice = hitDice;
        entity.description = description;
        entity.gameSystem = gameSystem;
        entity.casterType = casterType;
        entity.spellcastingAbility = spellcastingAbility;
        entity.savingThrows = new ArrayList<>(savingThrows);
        entity.skillChoicesCount = skillChoicesCount;
        entity.equipmentDescription = equipmentDescription;
        entity.startingGoldDice = startingGoldDice;
        entity.languageChoices = languageChoices;

        entity.multiclassPrereqsJson = gson.toJson(multiclassPrereqs);

        if (isSubclass && parentClassKey != null) {
            entity.subclassOf = parentClassKey;
        } else {
            entity.subclassOf = null;
        }

        entity.skillOptionsJson = !skillOptions.isEmpty() ? gson.toJson(skillOptions) : "[]";
        entity.startingItemsJson = !startingItems.isEmpty() ? gson.toJson(startingItems) : "[]";
        entity.languageKeysJson = !fixedLanguages.isEmpty() ? gson.toJson(fixedLanguages) : "[]";
        entity.progressionJson = !progression.isEmpty() ? gson.toJson(progression) : "[]";
        entity.spellKeysJson = !spellKeys.isEmpty() ? gson.toJson(spellKeys) : "[]";

        return entity;
    }

    public interface Callback {
        void onResult(boolean success);
    }

    public static class ClassProgressionRow {
        public int level;
        public int proficiencyBonus = 2;
        public int cantripsKnown = 0;
        public int slots1st = 0;
        public int slots2nd = 0;
        public int slots3rd = 0;
        public int slots4th = 0;
        public int slots5th = 0;
        public int slots6th = 0;
        public int slots7th = 0;
        public int slots8th = 0;
        public int slots9th = 0;

        public ClassProgressionRow(int level) {
            this.level = level;
        }

        public int getSlotForLevel(int slotLevel) {
            switch (slotLevel) {
                case 1: return slots1st;
                case 2: return slots2nd;
                case 3: return slots3rd;
                case 4: return slots4th;
                case 5: return slots5th;
                case 6: return slots6th;
                case 7: return slots7th;
                case 8: return slots8th;
                case 9: return slots9th;
                default: return 0;
            }
        }

        public void setSlotForLevel(int slotLevel, int value) {
            switch (slotLevel) {
                case 1: slots1st = value; break;
                case 2: slots2nd = value; break;
                case 3: slots3rd = value; break;
                case 4: slots4th = value; break;
                case 5: slots5th = value; break;
                case 6: slots6th = value; break;
                case 7: slots7th = value; break;
                case 8: slots8th = value; break;
                case 9: slots9th = value; break;
            }
        }
    }
}
