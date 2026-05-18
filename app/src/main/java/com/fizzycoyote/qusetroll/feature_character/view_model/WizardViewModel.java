package com.fizzycoyote.qusetroll.feature_character.view_model;

import android.content.Context;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.fizzycoyote.qusetroll.core.local_database.Open5eDatabase;
import com.fizzycoyote.qusetroll.core.local_database.PlayerCharacterDatabase;
import com.fizzycoyote.qusetroll.core.models.character.CharacterAttributesEntity;
import com.fizzycoyote.qusetroll.core.models.character.CharacterClassAssignmentEntity;
import com.fizzycoyote.qusetroll.core.models.character.CharacterCreationDTO;
import com.fizzycoyote.qusetroll.core.models.character.CharacterEntity;
import com.fizzycoyote.qusetroll.core.models.character.CharacterTraitEntity;
import com.fizzycoyote.qusetroll.core.models.open5e.character_class.CharacterClassEntity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WizardViewModel extends ViewModel {

    // Shared executor for background tasks
    public final ExecutorService executor = Executors.newSingleThreadExecutor();

    // LiveData for attribute updates (used by AttributesStepFragment)
    public final MutableLiveData<List<Integer>> attributesLiveData = new MutableLiveData<>(Arrays.asList(8, 8, 8, 8, 8, 8));
    public final MutableLiveData<String> errorLiveData = new MutableLiveData<>();

    // Base data
    public String gameSystem = "5e-2014";
    public String characterName = "";
    public String speciesKey = "";
    public String backgroundKey = "";
    public String alignmentKey = "";
    private boolean isEditMode = false;
    private long editingCharacterId = -1;

    // Equipment
    public List<CharacterCreationDTO.InventoryItemDTO> backgroundEquipment = new ArrayList<>();
    public List<CharacterCreationDTO.InventoryItemDTO> classEquipment = new ArrayList<>();
    public String backgroundEquipmentDescription = "";
    public int classStartingGold = 0;
    public int startingGold = 0;
    public int backgroundGold = 0;
    public String classEquipmentDescription = "";
    public List<CharacterCreationDTO.InventoryItemDTO> backgroundCustomItems = new ArrayList<>();
    public String classGoldDice = "5d4";
    public boolean useClassEquipment = true;

    // Languages
    public List<String> racialFixedLanguages = new ArrayList<>();
    public int racialLanguageChoices = 0;
    public List<String> backgroundFixedLanguages = new ArrayList<>();
    public int backgroundLanguageChoices = 0;
    public int bonusLanguagesFromInt = 0;
    public List<String> classSecretLanguages = new ArrayList<>();
    public List<String> chosenBonusLanguages = new ArrayList<>();

    // Skill proficiencies
    public List<String> backgroundSkillProficiencies = new ArrayList<>();
    public int classSkillChoices = 0;
    public List<String> classSkillOptions = new ArrayList<>();
    public List<String> chosenSkillProficiencies = new ArrayList<>();

    // Traits
    public List<CharacterTraitEntity> characterTraits = new ArrayList<>();

    // Spellcasting
    public int cantripsCount = 0;
    public int spellsKnownCount = 0;
    public int preparedCount = 0;
    public boolean isPreparedCaster = false;
    public String spellcastingAbility = "INT";
    public int spellcastingAbilityMod = 0;
    public List<String> chosenCantripKeys = new ArrayList<>();
    public List<String> chosenSpellKeys = new ArrayList<>();

    // Character images
    public String characterImagePath = null;
    public String characterThumbnailPath = null;

    // Attributes
    public List<Integer> baseAttributes = new ArrayList<>(Arrays.asList(8, 8, 8, 8, 8, 8));
    public List<Integer> attributes = new ArrayList<>(Arrays.asList(8, 8, 8, 8, 8, 8));
    public String attributeMethod = "STANDARD";

    // Racial / background bonuses
    public List<Integer> racialBonuses = new ArrayList<>(Arrays.asList(0, 0, 0, 0, 0, 0));
    public List<Integer> backgroundBonuses = new ArrayList<>(Arrays.asList(0, 0, 0, 0, 0, 0));

    // Classes (multiclass)
    public List<ClassAssignment> classAssignments = new ArrayList<>();

    public List<CharacterCreationDTO.InventoryItemDTO> getTotalEquipment() {
        List<CharacterCreationDTO.InventoryItemDTO> total = new ArrayList<>();
        total.addAll(backgroundEquipment);
        total.addAll(classEquipment);
        return total;
    }

    /**
     * Recalculates final attributes from base attributes + bonuses.
     * Also posts the result to attributesLiveData for observers.
     */
    public void recalcFinalAttributes() {
        List<Integer> finalAttrs = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            int val = baseAttributes.get(i) + racialBonuses.get(i) + backgroundBonuses.get(i);
            finalAttrs.add(val);
        }
        attributes = finalAttrs;
        attributesLiveData.postValue(new ArrayList<>(finalAttrs));
    }

    /**
     * Loads an existing character for editing.
     */
    public void setEditMode(long characterId, Context context) {
        this.isEditMode = true;
        this.editingCharacterId = characterId;

        executor.execute(() -> {
            try {
                PlayerCharacterDatabase db = PlayerCharacterDatabase.getInstance(context);
                Open5eDatabase open5eDb = Open5eDatabase.getInstance(context);
                CharacterEntity character = db.characterDao().getCharacterSync(characterId);
                if (character != null) {
                    this.gameSystem = character.gameSystem;
                    this.characterName = character.name;
                    this.speciesKey = character.speciesKey;
                    this.backgroundKey = character.backgroundKey;
                    this.alignmentKey = character.alignmentKey;

                    CharacterAttributesEntity attrs = db.attributesDao().getByCharacterId(characterId);
                    if (attrs != null) {
                        this.baseAttributes = Arrays.asList(
                                attrs.strength, attrs.dexterity, attrs.constitution,
                                attrs.intelligence, attrs.wisdom, attrs.charisma
                        );
                        this.attributes = new ArrayList<>(this.baseAttributes);
                        attributesLiveData.postValue(new ArrayList<>(this.attributes));
                    } else {
                        this.baseAttributes = Arrays.asList(10, 10, 10, 10, 10, 10);
                        this.attributes = new ArrayList<>(this.baseAttributes);
                    }

                    List<CharacterClassAssignmentEntity> assignments = db.classAssignmentDao().getByCharacterId(characterId);
                    this.classAssignments.clear();
                    for (CharacterClassAssignmentEntity ca : assignments) {
                        CharacterClassEntity cls = open5eDb.characterClassDao().getClassByKeySync(ca.classKey);
                        if (cls != null) {
                            this.classAssignments.add(new ClassAssignment(cls.key, cls.name, ca.level));
                        }
                    }
                }
            } catch (Exception e) {
                errorLiveData.postValue("Error loading character: " + e.getMessage());
            }
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executor.shutdownNow();
    }

    public boolean isEditMode() { return isEditMode; }
    public long getEditingCharacterId() { return editingCharacterId; }

    public static class ClassAssignment {
        public String classKey;
        public String className;
        public int level;
        public ClassAssignment(String key, String name, int lvl) {
            this.classKey = key;
            this.className = name;
            this.level = lvl;
        }
    }
}