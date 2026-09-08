package com.murkfeatherstudio.questroll.feature_campaign.view_model;


import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.murkfeatherstudio.questroll.core.local_database.CampaignDatabase;
import com.murkfeatherstudio.questroll.core.local_database.Open5eDatabase;
import com.murkfeatherstudio.questroll.core.local_database.PlayerCharacterDatabase;
import com.murkfeatherstudio.questroll.core.models.campaign.CampaignEntity;
import com.murkfeatherstudio.questroll.core.models.campaign.CampaignNoteEntity;
import com.murkfeatherstudio.questroll.core.models.character.CharacterAttributesEntity;
import com.murkfeatherstudio.questroll.core.models.character.CharacterClassAssignmentEntity;
import com.murkfeatherstudio.questroll.core.models.character.CharacterEntity;
import com.murkfeatherstudio.questroll.core.models.character.CharacterWithRelations;
import com.murkfeatherstudio.questroll.core.models.character.InventoryItemEntity;
import com.murkfeatherstudio.questroll.core.models.open5e.character_class.CharacterClassEntity;
import com.murkfeatherstudio.questroll.core.models.open5e.item.ItemEntity;
import com.murkfeatherstudio.questroll.core.repository.CharacterRepository;
import com.murkfeatherstudio.questroll.feature_dice.model.Dice;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * ViewModel for the Campaign Detail screen, managing character integration, notes,
 * equipment, and progression within a specific campaign context.
 */
public class CampaignDetailViewModel extends AndroidViewModel {

    private final Open5eDatabase open5eDb;
    private final CampaignDatabase campaignDb;
    private final PlayerCharacterDatabase playerDb;
    private final CharacterRepository repository;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private final MutableLiveData<Long> campaignIdLive = new MutableLiveData<>();
    private final MediatorLiveData<Boolean> canCastSpells = new MediatorLiveData<>();
    private final MediatorLiveData<Integer> effectiveAc = new MediatorLiveData<>();

    public final LiveData<CampaignEntity> campaign;
    public final LiveData<CharacterEntity> character;
    public final LiveData<CharacterWithRelations> characterWithRelations;
    public final LiveData<List<CampaignNoteEntity>> notes;

    // Dice State Persistence
    private final MutableLiveData<Map<String, Integer>> diceCounts = new MutableLiveData<>(new HashMap<>());
    private final MutableLiveData<String> lastRollResult = new MutableLiveData<>("");
    private final MutableLiveData<List<Dice>> rolledDice = new MutableLiveData<>(new ArrayList<>());

    public CampaignDetailViewModel(@NonNull Application application) {
        super(application);

        this.open5eDb = Open5eDatabase.getInstance(application);
        this.campaignDb = CampaignDatabase.getInstance(application);
        this.playerDb = PlayerCharacterDatabase.getInstance(application);
        this.repository = CharacterRepository.getInstance(application);

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
            return playerDb.characterDao().getCharacterWithRelations(c.characterId);
        });

        canCastSpells.setValue(false);

        effectiveAc.addSource(characterWithRelations, this::updateEffectiveAc);
        canCastSpells.addSource(characterWithRelations, this::updateCanCastSpellsAsync);

        notes = Transformations.switchMap(
                campaignIdLive,
                id -> campaignDb.campaignNoteDao().getForCampaign(id)
        );
    }

    private CharacterEntity currentCharacter() {
        CharacterWithRelations cwr = characterWithRelations.getValue();
        return (cwr != null) ? cwr.character : null;
    }

    private void updateEffectiveAc(CharacterWithRelations cwr) {
        if (cwr == null || cwr.attributes == null) {
            effectiveAc.setValue(null);
            return;
        }

        int unarmoredAc = 10 + cwr.attributes.dexterityMod;
        InventoryItemEntity armor = null;
        InventoryItemEntity shield = null;

        if (cwr.inventory != null) {
            for (InventoryItemEntity item : cwr.inventory) {
                if (!item.isEquipped) continue;
                if ("body".equals(item.slot)) armor = item;
                if ("off_hand".equals(item.slot)) shield = item;
            }
        }

        final InventoryItemEntity finalArmor = armor;
        final InventoryItemEntity finalShield = shield;
        final CharacterAttributesEntity attrs = cwr.attributes;

        executor.execute(() -> {
            int computedAc = unarmoredAc;

            if (finalArmor != null && finalArmor.itemKey != null) {
                ItemEntity armorEntity = open5eDb.itemDao().getByKeySync(finalArmor.itemKey);
                if (armorEntity != null) {
                    int acBase = extractAcFromItem(armorEntity);
                    if (acBase > 0) {
                        boolean addDex = getAddDexMod(armorEntity);
                        int maxDex = getMaxDexBonus(armorEntity);
                        computedAc = addDex ? acBase + Math.min(attrs.dexterityMod, maxDex) : acBase;
                    }
                }
            }

            if (finalShield != null && finalShield.itemKey != null) {
                ItemEntity shieldEntity = open5eDb.itemDao().getByKeySync(finalShield.itemKey);
                if (shieldEntity != null && isShield(shieldEntity)) {
                    computedAc += extractAcFromItem(shieldEntity);
                }
            }

            effectiveAc.postValue(computedAc);
        });
    }

    private boolean isShield(ItemEntity item) {
        return (item.categoryKey != null && item.categoryKey.toLowerCase().contains("shield"))
                || (item.categoryName != null && item.categoryName.toLowerCase().contains("shield"));
    }

    private int extractAcFromItem(ItemEntity item) {
        if (item == null || item.armorJson == null || item.armorJson.isEmpty()) return 0;
        try {
            JSONObject json = new JSONObject(item.armorJson);
            return json.optInt("ac_base", 0);
        } catch (JSONException e) {
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
            return json.optInt("ac_cap_dexmod", 99);
        } catch (JSONException e) {
            return 99;
        }
    }

    public void setExhaustion(int level) {
        CharacterEntity c = currentCharacter();
        if (c == null) return;
        repository.setExhaustion(c.id, level);
    }

    public void setInspiration(boolean has) {
        CharacterEntity c = currentCharacter();
        if (c == null) return;
        repository.setInspiration(c.id, has);
    }

    public void setCampaignId(long id) {
        campaignIdLive.setValue(id);
    }

    public long getCampaignId() {
        Long id = campaignIdLive.getValue();
        return id != null ? id : -1L;
    }

    public LiveData<Integer> getEffectiveAc() {
        return effectiveAc;
    }

    public void adjustGold(float delta) {
        CharacterEntity c = currentCharacter();
        if (c == null) return;
        repository.adjustGold(c.id, delta);
    }

    public String getCurrentGameSystem() {
        CampaignEntity c = campaign.getValue();
        return c != null ? c.gameSystem : "5e-2014";
    }

    public void setCurrentHp(int newHp) {
        CharacterEntity c = currentCharacter();
        if (c == null) return;
        repository.setCurrentHp(c.id, newHp);
    }

    public void setTemporaryHp(int tempHp) {
        CharacterEntity c = currentCharacter();
        if (c == null) return;
        repository.setTemporaryHp(c.id, tempHp);
    }

    public void setMaxHp(int maxHp) {
        CharacterEntity c = currentCharacter();
        if (c == null) return;
        repository.setMaxHp(c.id, maxHp);
    }

    public void assignCharacter(long characterId) {
        long campId = getCampaignId();
        executor.execute(() ->
                campaignDb.campaignDao().updateCharacterId(campId, characterId)
        );
    }

    public void removeCharacter() {
        long campId = getCampaignId();
        executor.execute(() ->
                campaignDb.campaignDao().updateCharacterId(campId, -1L)
        );
    }

    public void addNote(String title, String content) {
        CampaignNoteEntity note = new CampaignNoteEntity();
        note.campaignId = getCampaignId();
        note.title = title;
        note.content = content;
        note.createdAt = new Date();
        executor.execute(() -> campaignDb.campaignNoteDao().insert(note));
    }

    public void updateNote(CampaignNoteEntity note) {
        executor.execute(() -> campaignDb.campaignNoteDao().update(note));
    }

    public void deleteNote(CampaignNoteEntity note) {
        executor.execute(() -> campaignDb.campaignNoteDao().deleteById(note.id));
    }

    public LiveData<Boolean> getCanCastSpells() {
        return canCastSpells;
    }

    private void updateCanCastSpellsAsync(CharacterWithRelations cwr) {
        if (cwr == null) {
            canCastSpells.setValue(false);
            return;
        }

        // Broad check: if character has any spells known, they can cast spells
        if (cwr.spells != null && !cwr.spells.isEmpty()) {
            canCastSpells.setValue(true);
            return;
        }

        if (cwr.classAssignments == null || cwr.classAssignments.isEmpty()) {
            canCastSpells.setValue(false);
            return;
        }

        executor.execute(() -> {
            boolean canCast = false;
            for (CharacterClassAssignmentEntity assignment : cwr.classAssignments) {
                CharacterClassEntity classEntity = open5eDb.characterClassDao()
                        .getClassByKeySync(assignment.classKey);
                if (classEntity != null && classEntity.casterType != null &&
                        !"NONE".equalsIgnoreCase(classEntity.casterType)) {
                    canCast = true;
                    break;
                }
            }
            canCastSpells.postValue(canCast);
        });
    }

    // --- INVENTORY / SHOP ---

    public void buyItem(InventoryItemEntity item, float totalCost) {
        CharacterEntity c = currentCharacter();
        if (c == null) return;
        repository.buyItem(c.id, totalCost, item);
    }

    public void getItem(InventoryItemEntity item) {
        CharacterEntity c = currentCharacter();
        if (c == null) return;
        item.characterId = c.id;
        repository.addItem(item);
    }

    public void removeItem(long inventoryItemId) {
        repository.removeItem(inventoryItemId);
    }

    public void toggleEquipItem(long inventoryItemId, boolean equipped, String slot) {
        repository.toggleEquipItem(inventoryItemId, equipped, slot);
    }

    public LiveData<Map<String, Integer>> getDiceCounts() { return diceCounts; }
    public LiveData<String> getLastRollResult() { return lastRollResult; }
    public LiveData<List<Dice>> getRolledDice() { return rolledDice; }

    public void updateDiceState(Map<String, Integer> counts, List<Dice> dice, String result) {
        diceCounts.setValue(new HashMap<>(counts));
        rolledDice.setValue(new ArrayList<>(dice));
        lastRollResult.setValue(result);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executor.shutdownNow();
    }
}