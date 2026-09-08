package com.murkfeatherstudio.questroll.feature_character.view_model;

import android.content.Context;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.murkfeatherstudio.questroll.core.AppExecutors;
import com.murkfeatherstudio.questroll.core.local_database.Open5eDatabase;
import com.murkfeatherstudio.questroll.core.local_database.PlayerCharacterDatabase;
import com.murkfeatherstudio.questroll.core.models.character.*;
import com.murkfeatherstudio.questroll.core.models.open5e.character_class.CharacterClassEntity;
import com.murkfeatherstudio.questroll.core.models.open5e.species.SpeciesEntity;
import com.murkfeatherstudio.questroll.core.repository.CharacterRepository;
import com.murkfeatherstudio.questroll.feature_character.engine.CharacterEngine;
import java.util.*;

/**
 * ViewModel backing the standalone character sheet screen (CharacterSheetFragment).
 * Loads a single character's data including stats, attributes, traits, and inventory.
 */
public class CharacterSheetViewModel extends ViewModel {

    private final MutableLiveData<CharacterEntity> character = new MutableLiveData<>();
    private final MutableLiveData<String> speciesName = new MutableLiveData<>("-");
    private final MutableLiveData<List<String>> classNames = new MutableLiveData<>();
    private final MutableLiveData<Integer> currentHp = new MutableLiveData<>();
    private final MutableLiveData<Integer> maxHp = new MutableLiveData<>();
    private final MutableLiveData<Integer> armorClass = new MutableLiveData<>();
    private final MutableLiveData<Integer> initiative = new MutableLiveData<>();
    private final MutableLiveData<Map<String, Integer>> attributes = new MutableLiveData<>();
    private final MutableLiveData<String> fullImagePath = new MutableLiveData<>();
    
    private final MutableLiveData<List<CharacterTraitEntity>> traits = new MutableLiveData<>();
    private final MutableLiveData<List<InventoryItemEntity>> inventory = new MutableLiveData<>();
    private final MutableLiveData<List<String>> proficientSavingThrows = new MutableLiveData<>();
    private final MutableLiveData<Map<String, Integer>> skillBonuses = new MutableLiveData<>();

    private PlayerCharacterDatabase pcDb;
    private Open5eDatabase open5eDb;
    private CharacterEngine engine;
    private CharacterRepository characterRepository;
    private long characterId;

    /**
     * Initializes this ViewModel for a specific character.
     */
    public void init(long id, Context context) {
        this.characterId = id;
        pcDb = PlayerCharacterDatabase.getInstance(context);
        open5eDb = Open5eDatabase.getInstance(context);
        engine = new CharacterEngine(context);
        characterRepository = CharacterRepository.getInstance(context);
        loadData();
    }

    private void loadData() {
        AppExecutors.getInstance().diskIO().execute(() -> {
            CharacterEntity c = pcDb.characterDao().getCharacterSync(characterId);
            character.postValue(c);
            if (c != null) {
                // Core stats
                currentHp.postValue(c.currentHp);
                maxHp.postValue(engine.calculateMaxHp(characterId));
                armorClass.postValue(engine.getArmorClass(characterId));
                initiative.postValue(engine.getInitiative(characterId));
                fullImagePath.postValue(c.imagePath);

                // Race Name
                if (c.speciesKey != null) {
                    SpeciesEntity species = open5eDb.speciesDao().getByKeySync(c.speciesKey);
                    speciesName.postValue(species != null ? species.name : c.speciesKey);
                }

                // Attributes
                CharacterAttributesEntity attrs = pcDb.characterAttributesDao().getByCharacterId(characterId);
                if (attrs != null) {
                    Map<String, Integer> attrMap = new LinkedHashMap<>();
                    attrMap.put("STR", attrs.strength);
                    attrMap.put("DEX", attrs.dexterity);
                    attrMap.put("CON", attrs.constitution);
                    attrMap.put("INT", attrs.intelligence);
                    attrMap.put("WIS", attrs.wisdom);
                    attrMap.put("CHA", attrs.charisma);
                    attributes.postValue(attrMap);
                }

                // Classes with names
                List<CharacterClassAssignmentEntity> assignments = pcDb.classAssignmentDao().getByCharacterId(characterId);
                if (assignments != null) {
                    List<String> nameList = new ArrayList<>();
                    for (CharacterClassAssignmentEntity a : assignments) {
                        CharacterClassEntity cls = open5eDb.characterClassDao().getClassByKeySync(a.classKey);
                        nameList.add((cls != null ? cls.name : a.classKey) + " " + a.level);
                    }
                    classNames.postValue(nameList);
                }

                // Features and Equipment
                traits.postValue(pcDb.traitDao().getByCharacterId(characterId));
                inventory.postValue(pcDb.inventoryItemDao().getByCharacterId(characterId));

                // Mechanics
                proficientSavingThrows.postValue(engine.getSavingThrowProficiencies(characterId));
                skillBonuses.postValue(engine.getSkillBonuses(characterId));
            }
        });
    }

    /**
     * Updates current HP by a delta, ensuring it stays within [0, maxHp].
     */
    public void updateHp(int delta) {
        CharacterEntity c = character.getValue();
        if (c == null) return;
        
        int mHp = engine.calculateMaxHp(characterId);
        int nextHp = Math.max(0, Math.min(mHp, c.currentHp + delta));
        
        characterRepository.setCurrentHp(characterId, nextHp);
        c.currentHp = nextHp;
        currentHp.postValue(nextHp);
    }

    /**
     * Toggles inspiration state.
     */
    public void toggleInspiration() {
        CharacterEntity c = character.getValue();
        if (c == null) return;
        boolean newState = !c.hasInspiration;
        characterRepository.setInspiration(characterId, newState);
        c.hasInspiration = newState;
        character.postValue(c);
    }

    /**
     * Updates exhaustion level.
     */
    public void updateExhaustion(int delta) {
        CharacterEntity c = character.getValue();
        if (c == null) return;
        int nextLevel = Math.max(0, Math.min(6, c.exhaustionLevel + delta));
        characterRepository.setExhaustion(characterId, nextLevel);
        c.exhaustionLevel = nextLevel;
        character.postValue(c);
    }

    /**
     * Permanently deletes the character.
     */
    public void deleteCharacter() {
        characterRepository.deleteCharacter(characterId);
    }

    // Getters
    public LiveData<CharacterEntity> getCharacter() { return character; }
    public LiveData<String> getSpeciesName() { return speciesName; }
    public LiveData<Integer> getCurrentHp() { return currentHp; }
    public LiveData<Integer> getMaxHp() { return maxHp; }
    public LiveData<Integer> getArmorClass() { return armorClass; }
    public LiveData<Integer> getInitiative() { return initiative; }
    public LiveData<Map<String, Integer>> getAttributes() { return attributes; }
    public LiveData<String> getFullImagePath() { return fullImagePath; }
    public LiveData<List<String>> getClassNames() { return classNames; }
    public LiveData<List<CharacterTraitEntity>> getTraits() { return traits; }
    public LiveData<List<InventoryItemEntity>> getInventory() { return inventory; }
    public LiveData<List<String>> getProficientSavingThrows() { return proficientSavingThrows; }
    public LiveData<Map<String, Integer>> getSkillBonuses() { return skillBonuses; }
}
