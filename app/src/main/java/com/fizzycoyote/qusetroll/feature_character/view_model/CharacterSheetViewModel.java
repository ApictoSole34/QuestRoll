package com.fizzycoyote.qusetroll.feature_character.view_model;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.fizzycoyote.qusetroll.core.local_database.Open5eDatabase;
import com.fizzycoyote.qusetroll.core.local_database.PlayerCharacterDatabase;
import com.fizzycoyote.qusetroll.core.models.character.CharacterAttributesEntity;
import com.fizzycoyote.qusetroll.core.models.character.CharacterClassAssignmentEntity;
import com.fizzycoyote.qusetroll.core.models.character.CharacterEntity;
import com.fizzycoyote.qusetroll.core.models.character.CharacterLanguageEntity;
import com.fizzycoyote.qusetroll.core.models.character.CharacterSpellEntity;
import com.fizzycoyote.qusetroll.core.models.character.CharacterTraitEntity;
import com.fizzycoyote.qusetroll.core.models.character.InventoryItemEntity;
import com.fizzycoyote.qusetroll.core.models.open5e.ability.skill.SkillEntity;
import com.fizzycoyote.qusetroll.feature_character.engine.CharacterEngine;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * ViewModel for the Character Sheet, responsible for loading and preparing all data
 * required to display a character's details.
 * <p>
 * It coordinates data fetching from both the {@link PlayerCharacterDatabase} (user-specific data)
 * and the {@link Open5eDatabase} (game rules data), and uses the {@link CharacterEngine}
 * to calculate derived statistics.
 * </p>
 */
public class CharacterSheetViewModel extends ViewModel {

    private final MutableLiveData<CharacterEntity> character = new MutableLiveData<>();
    private final MutableLiveData<String> speciesName = new MutableLiveData<>();
    private final MutableLiveData<String> alignmentName = new MutableLiveData<>();
    private final MutableLiveData<String> backgroundName = new MutableLiveData<>();
    private final MutableLiveData<List<String>> classNames = new MutableLiveData<>();
    private final MutableLiveData<Integer> currentHp = new MutableLiveData<>();
    private final MutableLiveData<Integer> maxHp = new MutableLiveData<>();
    private final MutableLiveData<Integer> armorClass = new MutableLiveData<>();
    private final MutableLiveData<Integer> initiative = new MutableLiveData<>();
    private final MutableLiveData<Integer> proficiencyBonus = new MutableLiveData<>();
    private final MutableLiveData<Map<String, Integer>> attributes = new MutableLiveData<>();
    private final MutableLiveData<List<SkillDisplay>> skillBonuses = new MutableLiveData<>();
    private final MutableLiveData<String> fullImagePath = new MutableLiveData<>();

    // Additional LiveData for character details
    private final MutableLiveData<List<CharacterLanguageEntity>> languages = new MutableLiveData<>();
    private final MutableLiveData<List<CharacterTraitEntity>> traits = new MutableLiveData<>();
    private final MutableLiveData<List<InventoryItemEntity>> inventory = new MutableLiveData<>();
    private final MutableLiveData<List<CharacterSpellEntity>> spells = new MutableLiveData<>();

    private PlayerCharacterDatabase pcDb;
    private Open5eDatabase open5eDb;
    private CharacterEngine engine;
    private long characterId;

    /**
     * Simple data class to hold skill display information.
     */
    public static class SkillDisplay {
        public final String name;
        public final int bonus;
        public SkillDisplay(String name, int bonus) {
            this.name = name;
            this.bonus = bonus;
        }
    }

    /**
     * Initializes the ViewModel with a character ID and context.
     * Starts the data loading process.
     *
     * @param id      The ID of the character to load.
     * @param context Application context for database access.
     */
    public void init(long id, Context context) {
        this.characterId = id;
        pcDb = PlayerCharacterDatabase.getInstance(context);
        open5eDb = Open5eDatabase.getInstance(context);
        engine = new CharacterEngine(context);
        loadData();
    }

    /**
     * Loads all character-related data on a background thread.
     * Fetches core stats, attributes, skills, equipment, traits, and spells.
     */
    private void loadData() {
        new Thread(() -> {
            CharacterEntity c = pcDb.characterDao().getCharacterSync(characterId);
            character.postValue(c);
            if (c != null) {
                // Species
                if (c.speciesKey != null) {
                    var species = open5eDb.speciesDao().getByKeySync(c.speciesKey);
                    if (species != null) speciesName.postValue(species.name);
                }
                // Alignment
                if (c.alignmentKey != null) {
                    var align = open5eDb.alignmentDao().getByKeySync(c.alignmentKey);
                    if (align != null) alignmentName.postValue(align.shortName);
                }
                // Background
                if (c.backgroundKey != null) {
                    var bg = open5eDb.backgroundDao().getByKeySync(c.backgroundKey);
                    if (bg != null) backgroundName.postValue(bg.name);
                }
                // Classes
                List<CharacterClassAssignmentEntity> assignments = pcDb.classAssignmentDao().getByCharacterId(characterId);
                List<String> classNamesList = assignments.stream()
                        .map(a -> {
                            var cls = open5eDb.characterClassDao().getClassByKeySync(a.classKey);
                            return cls != null ? cls.name + " " + a.level : a.classKey;
                        })
                        .collect(Collectors.toList());
                classNames.postValue(classNamesList);

                currentHp.postValue(c.currentHp);
                int max = engine.calculateMaxHp(characterId);
                maxHp.postValue(max);
                armorClass.postValue(engine.getArmorClass(characterId));
                initiative.postValue(engine.getInitiative(characterId));
                int totalLevel = engine.getTotalLevel(characterId);
                proficiencyBonus.postValue(engine.getProficiencyBonus(totalLevel));

                CharacterAttributesEntity attrs = pcDb.characterAttributesDao().getByCharacterId(characterId);
                if (attrs != null) {
                    Map<String, Integer> attrMap = new HashMap<>();
                    attrMap.put("STR", attrs.strength);
                    attrMap.put("DEX", attrs.dexterity);
                    attrMap.put("CON", attrs.constitution);
                    attrMap.put("INT", attrs.intelligence);
                    attrMap.put("WIS", attrs.wisdom);
                    attrMap.put("CHA", attrs.charisma);
                    attributes.postValue(attrMap);
                }

                // Skills
                Map<String, Integer> bonusMap = engine.getSkillBonuses(characterId);
                List<SkillEntity> allSkills = open5eDb.skillDao().getAllSync();
                Map<String, String> keyToName = new HashMap<>();
                for (SkillEntity skill : allSkills) keyToName.put(skill.key, skill.name);
                List<SkillDisplay> displayList = new ArrayList<>();
                for (Map.Entry<String, Integer> entry : bonusMap.entrySet()) {
                    String key = entry.getKey();
                    if (key.startsWith("a5e_") || key.contains("a5e-ag")) continue;
                    String displayName = keyToName.getOrDefault(key, key);
                    int bonus = entry.getValue();
                    displayList.add(new SkillDisplay(displayName, bonus));
                }
                displayList.sort(Comparator.comparing(a -> a.name));
                skillBonuses.postValue(displayList);

                // Image
                fullImagePath.postValue(c.imagePath);

                // Languages
                List<CharacterLanguageEntity> langList = pcDb.languageDao().getByCharacterId(characterId);
                languages.postValue(langList);

                // Traits
                List<CharacterTraitEntity> traitList = pcDb.traitDao().getByCharacterId(characterId);
                traits.postValue(traitList);

                // Inventory
                List<InventoryItemEntity> invList = pcDb.inventoryItemDao().getByCharacterId(characterId);
                inventory.postValue(invList);

                // Spells
                List<CharacterSpellEntity> spellList = pcDb.spellDao().getByCharacterId(characterId);
                spells.postValue(spellList);
            }
        }).start();
    }

    // Getters
    public LiveData<CharacterEntity> getCharacter() { return character; }
    public LiveData<String> getSpeciesName() { return speciesName; }
    public LiveData<String> getAlignmentName() { return alignmentName; }
    public LiveData<String> getBackgroundName() { return backgroundName; }
    public LiveData<List<String>> getClassNames() { return classNames; }
    public LiveData<Integer> getCurrentHp() { return currentHp; }
    public LiveData<Integer> getMaxHp() { return maxHp; }
    public LiveData<Integer> getArmorClass() { return armorClass; }
    public LiveData<Integer> getInitiative() { return initiative; }
    public LiveData<Integer> getProficiencyBonus() { return proficiencyBonus; }
    public LiveData<Map<String, Integer>> getAttributes() { return attributes; }
    public LiveData<List<SkillDisplay>> getSkillBonuses() { return skillBonuses; }
    public LiveData<String> getFullImagePath() { return fullImagePath; }
    public LiveData<List<CharacterLanguageEntity>> getLanguages() { return languages; }
    public LiveData<List<CharacterTraitEntity>> getTraits() { return traits; }
    public LiveData<List<InventoryItemEntity>> getInventory() { return inventory; }
    public LiveData<List<CharacterSpellEntity>> getSpells() { return spells; }
}
