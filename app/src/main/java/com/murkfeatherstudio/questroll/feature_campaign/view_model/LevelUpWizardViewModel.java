package com.murkfeatherstudio.questroll.feature_campaign.view_model;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.murkfeatherstudio.questroll.core.config.SubclassLevelConfig;
import com.murkfeatherstudio.questroll.core.local_database.Open5eDatabase;
import com.murkfeatherstudio.questroll.core.local_database.PlayerCharacterDatabase;
import com.murkfeatherstudio.questroll.core.models.character.*;
import com.murkfeatherstudio.questroll.core.models.open5e.character_class.CharacterClassEntity;
import com.murkfeatherstudio.questroll.core.models.open5e.character_class.feature.FeatureEntity;
import com.murkfeatherstudio.questroll.core.models.open5e.character_class.gained_at.GainedAt;
import com.murkfeatherstudio.questroll.core.models.open5e.character_class.table_data.TableData;
import com.murkfeatherstudio.questroll.core.models.open5e.spell.SpellEntity;
import com.murkfeatherstudio.questroll.feature_character.engine.CharacterEngine;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * ViewModel for the Level Up Wizard.
 */
public class LevelUpWizardViewModel extends AndroidViewModel {

    private static final int[] XP_THRESHOLDS = {
            0, 300, 900, 2700, 6500, 14000, 23000, 34000, 48000,
            64000, 85000, 100000, 120000, 140000, 165000, 195000,
            225000, 265000, 305000, 355000
    };

    private static final int[] ASI_STANDARD = {4, 8, 12, 16, 19};
    private static final int[] ASI_FIGHTER  = {4, 6, 8, 12, 14, 16, 19};
    private static final int[] ASI_ROGUE    = {4, 8, 10, 12, 16, 19};

    public final MutableLiveData<Boolean> isLoading       = new MutableLiveData<>(false);
    public final MutableLiveData<String>  errorMessage    = new MutableLiveData<>();
    public final MutableLiveData<Boolean> levelUpComplete = new MutableLiveData<>(false);

    private final MutableLiveData<List<FeatureEntity>> featuresLive =
            new MutableLiveData<>(new ArrayList<>());

    private final MutableLiveData<List<CharacterClassEntity>> subclassesLive =
            new MutableLiveData<>(new ArrayList<>());

    public final MutableLiveData<Boolean> initDone = new MutableLiveData<>(false);

    public int     hitDiceSides  = 8;
    public int     conMod        = 0;
    public int     newLevel      = 1;
    public int     oldLevel      = 0;
    public boolean needsSubclass = false;
    public boolean needsAsi      = false;
    public boolean needsSpells   = false;
    public String  className     = "";

    public int    chosenHpGain       = 0;
    public int    lastRollValue      = 0;
    public String chosenSubclassKey  = null;
    public final List<String> asiIncreases = new ArrayList<>();

    private int newSpellsKnown = 0;
    private final MutableLiveData<List<SpellEntity>> availableSpellsLive = new MutableLiveData<>();
    private final MutableLiveData<Integer> spellsToChooseLive = new MutableLiveData<>(0);
    private List<String> selectedSpellKeys = new ArrayList<>();

    private long   characterId;
    private String classKey;
    private boolean isNewClass;

    private final PlayerCharacterDatabase playerDb;
    private final Open5eDatabase          open5eDb;
    private final ExecutorService         executor = Executors.newSingleThreadExecutor();
    private final Random rng = new Random();

    public LevelUpWizardViewModel(@NonNull Application application) {
        super(application);
        playerDb = PlayerCharacterDatabase.getInstance(application);
        open5eDb = Open5eDatabase.getInstance(application);
    }

    public void init(long characterId, String classKey, boolean isNewClass) {
        this.characterId = characterId;
        this.classKey    = classKey;
        this.isNewClass  = isNewClass;
        asiIncreases.clear();
        selectedSpellKeys.clear();
        chosenHpGain      = 0;
        chosenSubclassKey = null;

        isLoading.postValue(true);
        executor.execute(() -> {
            try {
                List<CharacterClassAssignmentEntity> assignments =
                        playerDb.classAssignmentDao().getByCharacterId(characterId);
                int currentClassLevel = 0;
                for (CharacterClassAssignmentEntity a : assignments) {
                    if (classKey.equals(a.classKey)) {
                        currentClassLevel = a.level;
                        break;
                    }
                }
                oldLevel = currentClassLevel;
                newLevel = isNewClass ? 1 : oldLevel + 1;

                CharacterClassEntity cls = open5eDb.characterClassDao().getClassByKeySync(classKey);
                if (cls != null) {
                    hitDiceSides = parseHitDie(cls.hitDice);
                    className    = cls.name != null ? cls.name : classKey;
                }

                CharacterAttributesEntity attrs =
                        playerDb.characterAttributesDao().getByCharacterId(characterId);
                conMod = (attrs != null) ? CharacterEngine.getAbilityModifier(attrs.constitution) : 0;

                needsAsi = isAsiLevel(classKey, newLevel);

                needsSubclass = false;
                int subclassLevel = SubclassLevelConfig.getSubclassLevel(classKey);
                boolean subclassUnlocked = (newLevel == subclassLevel);
                if (isNewClass && newLevel == 1 && subclassLevel == 1) subclassUnlocked = true;
                if (subclassUnlocked) {
                    CharacterSubclassAssignmentEntity existing =
                            playerDb.characterSubclassAssignmentDao().getForCharacterAndClass(characterId, classKey);
                    if (existing == null) {
                        List<CharacterClassEntity> subs =
                                open5eDb.characterClassDao().getSubclassesByParentKeySync(classKey);
                        if (subs != null && !subs.isEmpty()) {
                            needsSubclass = true;
                            subclassesLive.postValue(subs);
                        }
                    }
                }

                List<FeatureEntity> features = loadFeaturesForLevel(classKey, newLevel);
                featuresLive.postValue(features);

                int oldSpellsKnown = getSpellsKnownForLevel(classKey, oldLevel);
                int newSpellsKnownValue = getSpellsKnownForLevel(classKey, newLevel);
                newSpellsKnown = newSpellsKnownValue - oldSpellsKnown;
                if (newSpellsKnown > 0) {
                    needsSpells = true;
                    spellsToChooseLive.postValue(newSpellsKnown);
                    int maxSpellLevel = getMaxSpellLevelForClass(classKey, newLevel);
                    loadAvailableSpells(classKey, maxSpellLevel);
                } else {
                    needsSpells = false;
                }

                initDone.postValue(true);

            } catch (Exception e) {
                errorMessage.postValue("Init error: " + e.getMessage());
            } finally {
                isLoading.postValue(false);
            }
        });
    }

    private int getSpellsKnownForLevel(String classKey, int level) {
        List<FeatureEntity> features = open5eDb.featureDao().getFeaturesForClassSync(classKey);
        for (FeatureEntity f : features) {
            if ("Spells Known".equals(f.name) && f.tableData != null) {
                for (TableData td : f.tableData) {
                    if (td.level == level) {
                        try { return Integer.parseInt(td.columnValue); }
                        catch (NumberFormatException e) { return 0; }
                    }
                }
            }
        }
        return 0;
    }

    public List<String> getSelectedSpells() { return selectedSpellKeys; }

    private void loadAvailableSpells(String classKey, int maxSpellLevel) {
        executor.execute(() -> {
            String gameSystem = getCurrentGameSystem();
            List<SpellEntity> allSpells = open5eDb.spellDao().getAllByGameSystem(gameSystem);
            List<SpellEntity> available = new ArrayList<>();
            for (SpellEntity spell : allSpells) {
                if (spell.level <= maxSpellLevel && spell.classes != null && spell.classes.contains(classKey)) {
                    available.add(spell);
                }
            }
            availableSpellsLive.postValue(available);
        });
    }

    private String getCurrentGameSystem() {
        return "5e-2014";
    }

    public void setSelectedSpells(List<String> keys) { this.selectedSpellKeys = keys; }
    public LiveData<List<SpellEntity>> getAvailableSpells() { return availableSpellsLive; }
    public LiveData<Integer> getSpellsToChoose() { return spellsToChooseLive; }

    private int getMaxSpellLevelForClass(String classKey, int classLevel) {
        CharacterClassEntity cls = open5eDb.characterClassDao().getClassByKeySync(classKey);
        if (cls == null || cls.casterType == null || "NONE".equals(cls.casterType)) return 0;
        int maxSpellLevel = (classLevel + 1) / 2;
        return Math.min(maxSpellLevel, 9);
    }

    public int rollHp() {
        lastRollValue = rng.nextInt(hitDiceSides) + 1;
        chosenHpGain  = Math.max(1, lastRollValue + conMod);
        return chosenHpGain;
    }

    public int averageHp() {
        // Standard D&D 5e average: half of die + 1 (e.g., 5 for d8)
        int avg = (hitDiceSides / 2) + 1;
        lastRollValue = avg;
        chosenHpGain  = Math.max(1, avg + conMod);
        return chosenHpGain;
    }

    public void setAsiChoices(List<String> statKeys) {
        asiIncreases.clear();
        asiIncreases.addAll(statKeys);
    }

    public void confirmLevelUp(Runnable onSuccess) {
        if (chosenHpGain == 0) averageHp();
        isLoading.postValue(true);
        executor.execute(() -> {
            try {
                List<CharacterClassAssignmentEntity> assignments =
                        playerDb.classAssignmentDao().getByCharacterId(characterId);
                CharacterClassAssignmentEntity target = null;
                for (CharacterClassAssignmentEntity a : assignments) {
                    if (classKey.equals(a.classKey)) { target = a; break; }
                }
                if (target == null) {
                    target = new CharacterClassAssignmentEntity();
                    target.characterId = characterId;
                    target.classKey    = classKey;
                    target.level       = newLevel;
                    playerDb.classAssignmentDao().insert(target);
                } else {
                    target.level = newLevel;
                    playerDb.classAssignmentDao().update(target);
                }

                List<CharacterClassAssignmentEntity> fresh =
                        playerDb.classAssignmentDao().getByCharacterId(characterId);
                int totalLevel = 0;
                for (CharacterClassAssignmentEntity a : fresh) totalLevel += a.level;

                CharacterEntity character = playerDb.characterDao().getCharacterSync(characterId);
                if (character == null) {
                    errorMessage.postValue("Character not found.");
                    return;
                }
                character.totalLevel = totalLevel;
                character.maxHp     += chosenHpGain;
                character.currentHp  = Math.min(character.currentHp + chosenHpGain, character.maxHp);
                playerDb.characterDao().update(character);

                if (chosenSubclassKey != null) {
                    CharacterSubclassAssignmentEntity existing =
                            playerDb.characterSubclassAssignmentDao().getForCharacterAndClass(characterId, classKey);
                    if (existing == null) {
                        existing = new CharacterSubclassAssignmentEntity();
                        existing.characterId = characterId;
                        existing.classKey = classKey;
                    }
                    existing.subclassKey = chosenSubclassKey;
                    if (existing.id == 0) playerDb.characterSubclassAssignmentDao().insert(existing);
                    else playerDb.characterSubclassAssignmentDao().update(existing);
                }

                if (!asiIncreases.isEmpty()) applyAsi(characterId);

                addFeatureTraits(characterId, featuresLive.getValue(), totalLevel);

                if (!selectedSpellKeys.isEmpty()) {
                    for (String spellKey : selectedSpellKeys) {
                        CharacterSpellEntity spell = new CharacterSpellEntity();
                        spell.characterId = characterId;
                        spell.spellKey = spellKey;
                        playerDb.spellDao().insert(spell);
                    }
                }

                levelUpComplete.postValue(true);
                if (onSuccess != null) requireMainThread(onSuccess);

            } catch (Exception e) {
                errorMessage.postValue("Level up failed: " + e.getMessage());
            } finally {
                isLoading.postValue(false);
            }
        });
    }

    public void setSelectedSubclass(String key) { chosenSubclassKey = key; }
    public LiveData<List<FeatureEntity>> getFeatures() { return featuresLive; }
    public LiveData<List<CharacterClassEntity>> getAvailableSubclasses() { return subclassesLive; }

    private void applyAsi(long characterId) {
        CharacterAttributesEntity attrs = playerDb.characterAttributesDao().getByCharacterId(characterId);
        if (attrs == null) return;
        for (String stat : asiIncreases) {
            switch (stat) {
                case "strength": attrs.strength++; break;
                case "dexterity": attrs.dexterity++; break;
                case "constitution": attrs.constitution++; break;
                case "intelligence": attrs.intelligence++; break;
                case "wisdom": attrs.wisdom++; break;
                case "charisma": attrs.charisma++; break;
            }
        }
        attrs.strengthMod     = CharacterEngine.getAbilityModifier(attrs.strength);
        attrs.dexterityMod    = CharacterEngine.getAbilityModifier(attrs.dexterity);
        attrs.constitutionMod = CharacterEngine.getAbilityModifier(attrs.constitution);
        attrs.intelligenceMod = CharacterEngine.getAbilityModifier(attrs.intelligence);
        attrs.wisdomMod       = CharacterEngine.getAbilityModifier(attrs.wisdom);
        attrs.charismaMod     = CharacterEngine.getAbilityModifier(attrs.charisma);
        playerDb.characterAttributesDao().update(attrs);
    }

    private void addFeatureTraits(long characterId, List<FeatureEntity> features, int totalLevel) {
        if (features == null || features.isEmpty()) return;
        List<CharacterTraitEntity> toInsert = new ArrayList<>();
        List<CharacterTraitEntity> existing = playerDb.traitDao().getByCharacterId(characterId);
        for (FeatureEntity f : features) {
            boolean alreadyExists = false;
            for (CharacterTraitEntity t : existing) {
                if (classKey.equals(t.sourceKey) && f.name != null && f.name.equals(t.name)) {
                    alreadyExists = true;
                    break;
                }
            }
            if (alreadyExists) continue;
            CharacterTraitEntity t = new CharacterTraitEntity();
            t.characterId      = characterId;
            t.sourceType       = "CLASS";
            t.sourceKey        = classKey;
            t.name             = f.name != null ? f.name : "Unknown feature";
            t.description      = f.desc != null ? f.desc : "";
            t.levelRequirement = totalLevel;
            t.displayOrder     = existing.size() + toInsert.size();
            toInsert.add(t);
        }
        if (!toInsert.isEmpty()) playerDb.traitDao().insertAll(toInsert);
    }

    private List<FeatureEntity> loadFeaturesForLevel(String classKey, int level) {
        List<FeatureEntity> all = open5eDb.featureDao().getFeaturesForClassSync(classKey);
        List<FeatureEntity> result = new ArrayList<>();
        if (all == null) return result;
        for (FeatureEntity f : all) {
            if (f.gainedAt == null) continue;
            for (GainedAt ga : f.gainedAt) {
                if (ga.level == level) { result.add(f); break; }
            }
        }
        return result;
    }

    public static boolean isAsiLevel(String classKey, int classLevel) {
        int[] levels;
        String lower = classKey != null ? classKey.toLowerCase() : "";
        if (lower.contains("fighter"))      levels = ASI_FIGHTER;
        else if (lower.contains("rogue"))   levels = ASI_ROGUE;
        else                                levels = ASI_STANDARD;
        for (int l : levels) if (l == classLevel) return true;
        return false;
    }

    public static int parseHitDie(String hitDice) {
        if (hitDice == null) return 8;
        String s = hitDice.toLowerCase().trim();
        int d = s.lastIndexOf('d');
        if (d < 0) return 8;
        try {
            return Integer.parseInt(s.substring(d + 1).trim());
        } catch (NumberFormatException e) {
            return 8;
        }
    }

    public static int xpForLevel(int level) {
        if (level < 1) return 0;
        if (level > 20) return XP_THRESHOLDS[19];
        return XP_THRESHOLDS[level - 1];
    }

    public static boolean canLevelUp(int currentXp, int currentTotalLevel) {
        if (currentTotalLevel >= 20) return false;
        return currentXp >= XP_THRESHOLDS[currentTotalLevel];
    }

    public static int xpProgressPercent(int xp, int currentTotalLevel) {
        if (currentTotalLevel <= 0) return 0;
        if (currentTotalLevel >= 20) return 100;
        int from = XP_THRESHOLDS[currentTotalLevel - 1];
        int to   = XP_THRESHOLDS[currentTotalLevel];
        if (to <= from) return 100;
        return Math.max(0, Math.min(100, (int)(((float)(xp - from) / (to - from)) * 100f)));
    }

    private void requireMainThread(Runnable r) {
        new android.os.Handler(android.os.Looper.getMainLooper()).post(r);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executor.shutdownNow();
    }
}
