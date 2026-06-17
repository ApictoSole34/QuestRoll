package com.fizzycoyote.qusetroll.feature_character.view_model;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.fizzycoyote.qusetroll.core.local_database.Open5eDatabase;
import com.fizzycoyote.qusetroll.core.local_database.PlayerCharacterDatabase;
import com.fizzycoyote.qusetroll.core.models.character.CharacterAttributesEntity;
import com.fizzycoyote.qusetroll.core.models.character.CharacterClassAssignmentEntity;
import com.fizzycoyote.qusetroll.core.models.character.CharacterCreationDTO;
import com.fizzycoyote.qusetroll.core.models.character.CharacterEntity;
import com.fizzycoyote.qusetroll.core.models.character.CharacterMapper;
import com.fizzycoyote.qusetroll.core.models.open5e.alignment.AlignmentEntity;
import com.fizzycoyote.qusetroll.core.models.open5e.background.BackgroundEntity;
import com.fizzycoyote.qusetroll.core.models.open5e.character_class.CharacterClassEntity;
import com.fizzycoyote.qusetroll.core.models.open5e.species.SpeciesEntity;
import com.fizzycoyote.qusetroll.feature_character.utils.AttributeGenerator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CharacterCreatorViewModel extends AndroidViewModel {

    private final Open5eDatabase open5eDb;
    private final PlayerCharacterDatabase pcDb;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private final MutableLiveData<String> characterName = new MutableLiveData<>("");
    private final MutableLiveData<String> gameSystem = new MutableLiveData<>("5e-2014");
    private final MutableLiveData<AlignmentEntity> selectedAlignment = new MutableLiveData<>();
    private final MutableLiveData<BackgroundEntity> selectedBackground = new MutableLiveData<>();
    private final MutableLiveData<SpeciesEntity> selectedSpecies = new MutableLiveData<>();
    private final MutableLiveData<List<ClassAssignment>> classAssignments = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<Integer>> attributes = new MutableLiveData<>(Arrays.asList(8, 8, 8, 8, 8, 8));
    private final MutableLiveData<String> attributeMethod = new MutableLiveData<>("STANDARD");
    private final MutableLiveData<List<AlignmentEntity>> alignments = new MutableLiveData<>();
    private final MutableLiveData<List<BackgroundEntity>> backgrounds = new MutableLiveData<>();
    private final MutableLiveData<List<SpeciesEntity>> species = new MutableLiveData<>();
    private final MutableLiveData<List<CharacterClassEntity>> classes = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isSaving = new MutableLiveData<>(false);
    private final MutableLiveData<Long> savedCharacterId = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public CharacterCreatorViewModel(Application app) {
        super(app);
        open5eDb = Open5eDatabase.getInstance(app);
        pcDb = PlayerCharacterDatabase.getInstance(app);
        loadReferenceData();
    }

    private void loadReferenceData() {
        executor.execute(() -> {
            try {
                List<AlignmentEntity> alignList = open5eDb.alignmentDao().getAllSync();
                List<BackgroundEntity> backList = open5eDb.backgroundDao().getAll();
                List<SpeciesEntity> speciesList = open5eDb.speciesDao().getAllSync();
                List<CharacterClassEntity> classList = open5eDb.characterClassDao().getBaseClassesSync();
                alignments.postValue(alignList);
                backgrounds.postValue(backList);
                species.postValue(speciesList);
                classes.postValue(classList);
            } catch (Exception e) {
                errorMessage.postValue("Error loading data: " + e.getMessage());
            }
        });
    }

    // Getters
    public LiveData<List<AlignmentEntity>> getAlignments() { return alignments; }
    public LiveData<List<BackgroundEntity>> getBackgrounds() { return backgrounds; }
    public LiveData<List<SpeciesEntity>> getSpecies() { return species; }
    public LiveData<List<CharacterClassEntity>> getClasses() { return classes; }
    public LiveData<String> getCharacterName() { return characterName; }
    public LiveData<AlignmentEntity> getSelectedAlignment() { return selectedAlignment; }
    public LiveData<BackgroundEntity> getSelectedBackground() { return selectedBackground; }
    public LiveData<SpeciesEntity> getSelectedSpecies() { return selectedSpecies; }
    public LiveData<List<ClassAssignment>> getClassAssignments() { return classAssignments; }
    public LiveData<List<Integer>> getAttributes() { return attributes; }
    public LiveData<String> getAttributeMethod() { return attributeMethod; }
    public LiveData<Boolean> getIsSaving() { return isSaving; }
    public LiveData<Long> getSavedCharacterId() { return savedCharacterId; }
    public LiveData<String> getErrorMessage() { return errorMessage; }

    public void setCharacterName(String name) { characterName.setValue(name); }
    public void setSelectedAlignment(AlignmentEntity a) { selectedAlignment.setValue(a); }
    public void setSelectedBackground(BackgroundEntity b) { selectedBackground.setValue(b); }
    public void setSelectedSpecies(SpeciesEntity s) { selectedSpecies.setValue(s); }

    public void addClass(CharacterClassEntity clazz, int level) {
        List<ClassAssignment> current = classAssignments.getValue();
        if (current == null) current = new ArrayList<>();
        current.add(new ClassAssignment(clazz.key, clazz.name, level));
        classAssignments.setValue(current);
    }

    public void removeClass(int index) {
        List<ClassAssignment> current = classAssignments.getValue();
        if (current != null && index >= 0 && index < current.size()) {
            current.remove(index);
            classAssignments.setValue(current);
        }
    }

    public void generateAttributes(String method) {
        attributeMethod.setValue(method);
        List<Integer> newStats;
        switch (method) {
            case "ROLL":
                newStats = AttributeGenerator.roll4d6DropLowest();
                break;
            case "POINT_BUY":
                newStats = AttributeGenerator.getPointBuyArray();
                break;
            default:
                newStats = AttributeGenerator.getStandardArray();
                break;
        }
        attributes.setValue(newStats);
    }

    public void adjustAttributeForPointBuy(int index, int delta) {
        if (!"POINT_BUY".equals(attributeMethod.getValue())) return;
        List<Integer> current = new ArrayList<>(attributes.getValue());
        int oldVal = current.get(index);
        int newVal = oldVal + delta;
        if (newVal < 8 || newVal > 15) return;
        int totalCost = 0;
        for (int i = 0; i < 6; i++) {
            totalCost += AttributeGenerator.getPointCost(i == index ? newVal : current.get(i));
        }
        if (totalCost > 27) return;
        current.set(index, newVal);
        attributes.setValue(current);
    }

    public void saveCharacter() {
        if (Boolean.TRUE.equals(isSaving.getValue())) return;
        isSaving.setValue(true);

        executor.execute(() -> {
            try {
                CharacterCreationDTO dto = new CharacterCreationDTO();
                dto.name = characterName.getValue();
                dto.gameSystem = gameSystem.getValue();
                dto.alignmentKey = selectedAlignment.getValue() != null ? selectedAlignment.getValue().key : null;
                dto.backgroundKey = selectedBackground.getValue() != null ? selectedBackground.getValue().key : null;
                dto.speciesKey = selectedSpecies.getValue() != null ? selectedSpecies.getValue().key : null;

                List<Integer> attrVals = attributes.getValue();
                Map<String, Integer> attrMap = new HashMap<>();
                String[] keys = {"STR", "DEX", "CON", "INT", "WIS", "CHA"};
                for (int i = 0; i < keys.length; i++) {
                    attrMap.put(keys[i], attrVals != null ? attrVals.get(i) : 8);
                }
                dto.attributes = attrMap;

                List<CharacterCreationDTO.ClassAssignmentDTO> classDTOs = new ArrayList<>();
                List<ClassAssignment> assignments = classAssignments.getValue();
                if (assignments != null) {
                    for (ClassAssignment ca : assignments) {
                        CharacterCreationDTO.ClassAssignmentDTO cdto = new CharacterCreationDTO.ClassAssignmentDTO();
                        cdto.classKey = ca.classKey;
                        cdto.level = ca.level;
                        classDTOs.add(cdto);
                    }
                }
                dto.classAssignments = classDTOs;
                dto.startingItems = new ArrayList<>();
                dto.startingTraits = new ArrayList<>();
                dto.startingSpellKeys = new ArrayList<>();

                CharacterEntity character = CharacterMapper.toEntity(dto);
                long charId = pcDb.characterDao().insert(character);

                CharacterAttributesEntity attrs = CharacterMapper.toAttributesEntity(charId, dto);
                pcDb.characterAttributesDao().insert(attrs);

                List<CharacterClassAssignmentEntity> classAssigns = CharacterMapper.toClassAssignments(charId, dto);
                pcDb.classAssignmentDao().insertAll(classAssigns);

                savedCharacterId.postValue(charId);
            } catch (Exception e) {
                errorMessage.postValue("Save error: " + e.getMessage());
                savedCharacterId.postValue(-1L);
            } finally {
                isSaving.postValue(false);
            }
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executor.shutdownNow();
    }

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