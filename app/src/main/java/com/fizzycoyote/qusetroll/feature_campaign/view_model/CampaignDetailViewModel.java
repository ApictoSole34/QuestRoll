package com.fizzycoyote.qusetroll.feature_campaign.view_model;


import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.fizzycoyote.qusetroll.core.local_database.CampaignDatabase;
import com.fizzycoyote.qusetroll.core.local_database.Open5eDatabase;
import com.fizzycoyote.qusetroll.core.local_database.PlayerCharacterDatabase;
import com.fizzycoyote.qusetroll.core.models.campaign.CampaignEntity;
import com.fizzycoyote.qusetroll.core.models.campaign.CampaignNoteEntity;
import com.fizzycoyote.qusetroll.core.models.character.CharacterAttributesEntity;
import com.fizzycoyote.qusetroll.core.models.character.CharacterClassAssignmentEntity;
import com.fizzycoyote.qusetroll.core.models.character.CharacterEntity;
import com.fizzycoyote.qusetroll.core.models.character.CharacterWithRelations;
import com.fizzycoyote.qusetroll.core.models.character.InventoryItemEntity;
import com.fizzycoyote.qusetroll.core.models.open5e.character_class.CharacterClassEntity;
import com.fizzycoyote.qusetroll.core.models.open5e.character_class.feature.FeatureEntity;
import com.fizzycoyote.qusetroll.core.models.open5e.character_class.gained_at.GainedAt;
import com.fizzycoyote.qusetroll.core.models.open5e.item.ItemEntity;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CampaignDetailViewModel extends AndroidViewModel {

    private final Open5eDatabase open5eDb;
    private final CampaignDatabase campaignDb;
    private final PlayerCharacterDatabase playerDb;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private final MutableLiveData<Long> campaignIdLive = new MutableLiveData<>();
    private final MutableLiveData<Boolean> canCastSpellsLive = new MutableLiveData<>(false);
    private final MediatorLiveData<Integer> effectiveAc = new MediatorLiveData<>();

    private List<ItemEntity> cachedShopItems = null;
    private final MutableLiveData<List<ItemEntity>> shopItemsLive = new MutableLiveData<>();

    public final LiveData<CampaignEntity> campaign;
    public final LiveData<CharacterEntity> character;
    public final LiveData<CharacterWithRelations> characterWithRelations;
    public final LiveData<List<CampaignNoteEntity>> notes;

    public CampaignDetailViewModel(@NonNull Application application) {
        super(application);

        this.open5eDb = Open5eDatabase.getInstance(application);
        this.campaignDb = CampaignDatabase.getInstance(application);
        this.playerDb = PlayerCharacterDatabase.getInstance(application);

        campaign = Transformations.switchMap(
                campaignIdLive,
                id -> campaignDb.campaignDao().getById(id)
        );

        character = Transformations.switchMap(campaign, c -> {
            if (c == null || c.characterId == -1) {
                return new MutableLiveData<>(null);
            }
            return playerDb.characterDao().getCharacter(c.characterId);
        });

        characterWithRelations = Transformations.switchMap(campaign, c -> {
            if (c == null || c.characterId == -1) {
                return new MutableLiveData<>(null);
            }
            return playerDb.characterDao()
                    .getCharacterWithRelations(c.characterId);
        });

        effectiveAc.addSource(characterWithRelations, cwr -> {
            updateEffectiveAc(cwr);
            updateCanCastSpellsAsync(cwr);
        });

        notes = Transformations.switchMap(
                campaignIdLive,
                id -> campaignDb.campaignNoteDao().getForCampaign(id)
        );
    }

    //------------ Dex + AC- -------

    private void updateEffectiveAc(CharacterWithRelations cwr) {
        if (cwr == null || cwr.attributes == null) {
            effectiveAc.setValue(null);
            return;
        }

        int unarmoredAc = 10 + cwr.attributes.dexterityMod;
        InventoryItemEntity armor = null;

        if (cwr.inventory != null) {
            for (InventoryItemEntity item : cwr.inventory) {
                if (item.isEquipped && "body".equals(item.slot)) {
                    armor = item;
                    break;
                }
            }
        }

        if (armor != null && armor.itemKey != null) {
            final InventoryItemEntity finalArmor = armor;
            final CharacterAttributesEntity attrs = cwr.attributes;
            executor.execute(() -> {
                ItemEntity itemEntity = Open5eDatabase.getInstance(getApplication())
                        .itemDao().getByKeySync(finalArmor.itemKey);
                int computedAc = unarmoredAc;
                if (itemEntity != null) {
                    int acBase   = extractAcFromItem(itemEntity);
                    boolean addDex = getAddDexMod(itemEntity);
                    int maxDex   = getMaxDexBonus(itemEntity);

                    if (acBase > 0) {
                        if (addDex) {
                            computedAc = acBase + Math.min(attrs.dexterityMod, maxDex);
                        } else {
                            computedAc = acBase; // np. Plate armor
                        }
                    }
                }
                effectiveAc.postValue(computedAc);
            });
        } else {
            effectiveAc.setValue(unarmoredAc);
        }
    }

    //----------------------------------------------- LEVEL UP ----------------------------------------------------
    public void levelUp(long characterId, String classKey, boolean isNewClass) {
        executor.execute(() -> {
            CharacterEntity character = playerDb.characterDao().getCharacterSync(characterId);
            if (character == null) return;

            List<CharacterClassAssignmentEntity> classes =
                    playerDb.classAssignmentDao().getByCharacterId(characterId);

            CharacterClassAssignmentEntity target = null;
            for (CharacterClassAssignmentEntity c : classes) {
                if (c.classKey.equals(classKey)) {
                    target = c;
                    break;
                }
            }

            boolean isNew = false;
            if (target == null) {
                target = new CharacterClassAssignmentEntity();
                target.characterId = characterId;
                target.classKey = classKey;
                target.level = 0;
                isNew = true;
            }

            int oldLevel = target.level;
            target.level = isNewClass ? 1 : target.level + 1;
            int levelGain = target.level - oldLevel;

            CharacterAttributesEntity attrs = playerDb.characterAttributesDao().getByCharacterId(characterId);
            int conMod = (attrs != null) ? attrs.constitutionMod : 0;
            int hitDice = getHitDiceSize(target.classKey);
            int hpGain = ((hitDice / 2) + 1 + conMod) * levelGain;
            if (hpGain < 1) hpGain = 1;
            character.maxHp += hpGain;

            if (isNew) {
                playerDb.classAssignmentDao().insert(target);
            } else {
                playerDb.classAssignmentDao().update(target);
            }

            int totalLevel = 0;
            List<CharacterClassAssignmentEntity> updatedClasses =
                    playerDb.classAssignmentDao().getByCharacterId(characterId);
            for (CharacterClassAssignmentEntity c : updatedClasses) {
                totalLevel += c.level;
            }
            character.totalLevel = totalLevel;

            playerDb.characterDao().update(character);

            refreshCampaignData();
        });
    }

    private int getHitDiceSize(String classKey) {
        switch (classKey.toLowerCase()) {
            case "barbarian": return 12;
            case "fighter": case "paladin": case "ranger": return 10;
            case "cleric": case "druid": case "monk": case "rogue": case "warlock": return 8;
            case "bard": case "sorcerer": case "wizard": return 6;
            default: return 8;
        }
    }

    private void refreshCampaignData() {
        long id = getCampaignId();
        if (id != -1) {
            campaignIdLive.postValue(id);
        }
    }

    public List<FeatureEntity> getFeaturesForClassAndLevelSync(String classKey, int level) {
        List<FeatureEntity> all = open5eDb.featureDao().getFeaturesForClassSync(classKey);
        List<FeatureEntity> result = new ArrayList<>();
        if (all == null) return result;
        for (FeatureEntity f : all) {
            if (f.gainedAt != null) {
                for (GainedAt ga : f.gainedAt) {
                    if (ga.level == level) {
                        result.add(f);
                        break;
                    }
                }
            }
        }
        return result;
    }

    public void levelUpWithFeatures(long characterId, String classKey, boolean isNewClass, LevelUpCallback callback) {
        executor.execute(() -> {
            CharacterEntity character = playerDb.characterDao().getCharacterSync(characterId);
            if (character == null) return;

            List<CharacterClassAssignmentEntity> classes = playerDb.classAssignmentDao().getByCharacterId(characterId);
            CharacterClassAssignmentEntity target = null;
            for (CharacterClassAssignmentEntity c : classes) {
                if (c.classKey.equals(classKey)) {
                    target = c;
                    break;
                }
            }

            boolean isNew = false;
            if (target == null) {
                target = new CharacterClassAssignmentEntity();
                target.characterId = characterId;
                target.classKey = classKey;
                target.level = 0;
                isNew = true;
            }

            int oldLevel = target.level;
            int newLevel = isNewClass ? 1 : oldLevel + 1;
            target.level = newLevel;
            int levelGain = newLevel - oldLevel;

            CharacterAttributesEntity attrs = playerDb.characterAttributesDao().getByCharacterId(characterId);
            int conMod = (attrs != null) ? attrs.constitutionMod : 0;
            int hitDice = getHitDiceSize(classKey);
            int hpGain = ((hitDice / 2) + 1 + conMod) * levelGain;
            if (hpGain < 1) hpGain = 1;
            character.maxHp += hpGain;

            if (isNew) {
                playerDb.classAssignmentDao().insert(target);
            } else {
                playerDb.classAssignmentDao().update(target);
            }

            int totalLevel = 0;
            List<CharacterClassAssignmentEntity> updatedClasses = playerDb.classAssignmentDao().getByCharacterId(characterId);
            for (CharacterClassAssignmentEntity c : updatedClasses) {
                totalLevel += c.level;
            }
            character.totalLevel = totalLevel;
            playerDb.characterDao().update(character);

            List<FeatureEntity> features = new ArrayList<>();
            if (isNewClass) {
                features.addAll(getFeaturesForClassAndLevelSync(classKey, 1));
            } else {
                features.addAll(getFeaturesForClassAndLevelSync(classKey, newLevel));
            }

            refreshCampaignData();

            if (callback != null) {
                new android.os.Handler(android.os.Looper.getMainLooper()).post(() ->
                        callback.onComplete(newLevel, features)
                );
            }
        });
    }

    public void increaseAttribute(long characterId, String attribute, int increase) {
        executor.execute(() -> {
            CharacterAttributesEntity attrs = playerDb.characterAttributesDao().getByCharacterId(characterId);
            if (attrs == null) return;

            switch (attribute.toLowerCase()) {
                case "strength": attrs.strength += increase; break;
                case "dexterity": attrs.dexterity += increase; break;
                case "constitution": attrs.constitution += increase; break;
                case "intelligence": attrs.intelligence += increase; break;
                case "wisdom": attrs.wisdom += increase; break;
                case "charisma": attrs.charisma += increase; break;
                default: return;
            }
            attrs.strengthMod = calcModifier(attrs.strength);
            attrs.dexterityMod = calcModifier(attrs.dexterity);
            attrs.constitutionMod = calcModifier(attrs.constitution);
            attrs.intelligenceMod = calcModifier(attrs.intelligence);
            attrs.wisdomMod = calcModifier(attrs.wisdom);
            attrs.charismaMod = calcModifier(attrs.charisma);

            playerDb.characterAttributesDao().update(attrs);
            refreshCampaignData();
        });
    }

    public interface LevelUpCallback {
        void onComplete(int newLevel, List<FeatureEntity> newFeatures);
    }


    //-------------------------------------------------------------------------------------------------------------

    private int extractAcFromItem(ItemEntity item) {
        if (item == null || item.armorJson == null || item.armorJson.isEmpty()) return 0;
        try {
            JSONObject json = new JSONObject(item.armorJson);
            return json.optInt("ac_base", 0);
        } catch (JSONException e) {
            e.printStackTrace();
            return 0;
        }
    }

    private boolean getAddDexMod(ItemEntity item) {
        if (item == null || item.armorJson == null) return true;
        try {
            JSONObject json = new JSONObject(item.armorJson);
            return json.optBoolean("ac_add_dexmod", true);
        } catch (JSONException e) {
            return true;
        }
    }

    private int getMaxDexBonus(ItemEntity item) {
        if (item == null || item.armorJson == null) return 99;
        try {
            JSONObject json = new JSONObject(item.armorJson);
            if (json.isNull("ac_cap_dexmod")) return 99;
            return json.optInt("ac_cap_dexmod", 99);
        } catch (JSONException e) {
            return 99;
        }
    }

    public LiveData<List<ItemEntity>> getShopItems() {
        return shopItemsLive;
    }

    public void loadShopItemsIfNeeded() {
        if (cachedShopItems != null) {
            shopItemsLive.setValue(cachedShopItems);
            return;
        }
        executor.execute(() -> {
            String gameSystem = getCurrentGameSystem();
            List<ItemEntity> items = Open5eDatabase.getInstance(getApplication())
                    .itemDao().getAllByGameSystem(gameSystem);
            cachedShopItems = items;
            shopItemsLive.postValue(items);
        });
    }

    public void setExhaustion(int level) {
        CharacterEntity c = character.getValue();
        if (c == null) return;
        executor.execute(() -> {
            c.exhaustionLevel = level;
            playerDb.characterDao().update(c);
        });
    }

    public void setInspiration(boolean has) {
        CharacterEntity c = character.getValue();
        if (c == null) return;
        executor.execute(() -> {
            c.hasInspiration = has;
            playerDb.characterDao().update(c);
        });
    }

    public void setCampaignId(long id) {
        Long current = campaignIdLive.getValue();

        if (current == null || current != id) {
            campaignIdLive.setValue(id);
        }
    }

    public long getCampaignId() {
        Long id = campaignIdLive.getValue();
        return id != null ? id : -1L;
    }

    public LiveData<Integer> getEffectiveAc() {
        return effectiveAc;
    }

    public void addItemToInventory(InventoryItemEntity item) {
        executor.execute(() -> playerDb.inventoryItemDao().insert(item));
    }

    public void adjustGold(float delta) {
        CharacterWithRelations cwr = characterWithRelations.getValue();
        if (cwr == null || cwr.character == null) return;
        executor.execute(() -> {
            CharacterEntity c = cwr.character;
            c.currentGold = Math.max(0f, c.currentGold + delta);
            playerDb.characterDao().update(c);
        });
    }

    public void setGold(float amount) {
        CharacterWithRelations cwr = characterWithRelations.getValue();
        if (cwr == null || cwr.character == null) return;
        executor.execute(() -> {
            CharacterEntity c = cwr.character;
            c.currentGold = Math.max(0f, amount);
            playerDb.characterDao().update(c);
        });
    }

    public void equipItem(long itemId, String slot) {
        executor.execute(() -> {
            InventoryItemEntity item = playerDb.inventoryItemDao().getById(itemId);
            if (item != null) {
                item.isEquipped = true;
                item.slot = slot;
                playerDb.inventoryItemDao().update(item);
            }
        });
    }

    public void unequipItem(long itemId) {
        executor.execute(() -> {
            InventoryItemEntity item = playerDb.inventoryItemDao().getById(itemId);
            if (item != null) {
                item.isEquipped = false;
                item.slot = null;
                playerDb.inventoryItemDao().update(item);
            }
        });
    }

    public String getCurrentGameSystem() {
        CampaignEntity c = campaign.getValue();
        return c != null ? c.gameSystem : "5e-2014";
    }

    // ---------- HP ----------

    public void setCurrentHp(int newHp) {
        CharacterEntity c = character.getValue();

        if (c == null) return;

        executor.execute(() -> {
            c.currentHp = newHp;
            playerDb.characterDao().update(c);
        });
    }

    public void setTemporaryHp(int tempHp) {
        CharacterEntity c = character.getValue();

        if (c == null) return;

        executor.execute(() -> {
            c.temporaryHp = tempHp;
            playerDb.characterDao().update(c);
        });
    }

    public void setMaxHp(int maxHp) {
        CharacterEntity c = character.getValue();

        if (c == null) return;

        executor.execute(() -> {
            c.maxHp = maxHp;
            playerDb.characterDao().update(c);
        });
    }

    // ---------- Campaign Character ----------

    public void assignCharacter(long characterId) {
        long campId = getCampaignId();

        executor.execute(() ->
                campaignDb.campaignDao()
                        .updateCharacterId(campId, characterId)
        );
    }

    public void removeCharacter() {
        long campId = getCampaignId();

        executor.execute(() ->
                campaignDb.campaignDao()
                        .updateCharacterId(campId, -1L)
        );
    }

    // ---------- Notes ----------

    public void addNote(String title, String content) {
        CampaignNoteEntity note = new CampaignNoteEntity();

        note.campaignId = getCampaignId();
        note.title = title;
        note.content = content;

        executor.execute(() ->
                campaignDb.campaignNoteDao().insert(note)
        );
    }

    public void updateNote(CampaignNoteEntity note) {
        executor.execute(() ->
                campaignDb.campaignNoteDao().update(note)
        );
    }

    public void deleteNote(CampaignNoteEntity note) {
        executor.execute(() ->
                campaignDb.campaignNoteDao().deleteById(note.id)
        );
    }

    // ---------- Inventory ----------

    public void toggleItemEquipped(long itemId, boolean equipped) {
        executor.execute(() -> {

            InventoryItemEntity item =
                    playerDb.inventoryItemDao().getById(itemId);

            if (item != null) {
                item.isEquipped = equipped;
                playerDb.inventoryItemDao().update(item);
            }
        });
    }

    public void deleteItem(long itemId) {
        executor.execute(() ->
                playerDb.inventoryItemDao().deleteItem(itemId)
        );
    }

    //-------- Cheack if char can have spells -------------------------

    public LiveData<Boolean> getCanCastSpells() {
        return canCastSpellsLive;
    }

    private void updateCanCastSpellsAsync(CharacterWithRelations cwr) {
        if (cwr == null || cwr.classAssignments == null) {
            canCastSpellsLive.postValue(false);
            return;
        }
        executor.execute(() -> {
            boolean canCast = false;
            for (CharacterClassAssignmentEntity assignment : cwr.classAssignments) {
                CharacterClassEntity classEntity = open5eDb.characterClassDao()
                        .getClassByKeySync(assignment.classKey);
                if (classEntity != null) {
                    if (classEntity.casterType != null && !"NONE".equals(classEntity.casterType)) {
                        canCast = true;
                        break;
                    }
                    if (isSpellcastingClass(assignment.classKey)) {
                        canCast = true;
                        break;
                    }
                }
            }
            final boolean finalCanCast = canCast;
            new android.os.Handler(android.os.Looper.getMainLooper()).post(() ->
                    canCastSpellsLive.setValue(finalCanCast)
            );
        });
    }

    private boolean isSpellcastingClass(String classKey) {
        String lower = classKey.toLowerCase();
        return lower.contains("sorcerer") || lower.contains("warlock") || lower.contains("cleric") ||
                lower.contains("wizard") || lower.contains("druid") || lower.contains("bard") ||
                lower.contains("paladin") || lower.contains("ranger");
    }

    // ---------- Helpers ----------

    public static int calcProficiencyBonus(int totalLevel) {
        return 1 + (int) Math.ceil(totalLevel / 4.0);
    }

    public static int calcModifier(int score) {
        return (int) Math.floor((score - 10) / 2.0);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executor.shutdownNow();
    }
}