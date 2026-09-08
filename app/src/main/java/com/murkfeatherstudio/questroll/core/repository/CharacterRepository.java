package com.murkfeatherstudio.questroll.core.repository;

import android.content.Context;
import androidx.lifecycle.LiveData;

import com.murkfeatherstudio.questroll.core.local_database.Open5eDatabase;
import com.murkfeatherstudio.questroll.core.local_database.PlayerCharacterDatabase;
import com.murkfeatherstudio.questroll.core.models.character.CharacterAttributesEntity;
import com.murkfeatherstudio.questroll.core.models.character.CharacterClassAssignmentEntity;
import com.murkfeatherstudio.questroll.core.models.character.CharacterDao;
import com.murkfeatherstudio.questroll.core.models.character.CharacterEntity;
import com.murkfeatherstudio.questroll.core.models.character.CharacterLanguageEntity;
import com.murkfeatherstudio.questroll.core.models.character.CharacterSpellEntity;
import com.murkfeatherstudio.questroll.core.models.character.CharacterTraitEntity;
import com.murkfeatherstudio.questroll.core.models.character.CharacterWithRelations;
import com.murkfeatherstudio.questroll.core.models.character.InventoryItemDao;
import com.murkfeatherstudio.questroll.core.models.character.InventoryItemEntity;
import com.murkfeatherstudio.questroll.feature_character.engine.CharacterEngine;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Repository that serves as the single source of truth for character data.
 */
public class CharacterRepository {
    private static volatile CharacterRepository INSTANCE;

    private final CharacterDao characterDao;
    private final InventoryItemDao inventoryDao;
    private final PlayerCharacterDatabase pcDb;
    private final Open5eDatabase open5eDb;
    private final CharacterEngine engine;
    private final Executor executor;

    private CharacterRepository(Context context) {
        this.pcDb = PlayerCharacterDatabase.getInstance(context);
        this.open5eDb = Open5eDatabase.getInstance(context);
        this.characterDao = pcDb.characterDao();
        this.inventoryDao = pcDb.inventoryItemDao();
        this.engine = new CharacterEngine(context);
        this.executor = Executors.newSingleThreadExecutor();
    }

    public static CharacterRepository getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (CharacterRepository.class) {
                if (INSTANCE == null) {
                    INSTANCE = new CharacterRepository(context.getApplicationContext());
                }
            }
        }
        return INSTANCE;
    }

    // --- GOLD MANAGEMENT ---

    public void adjustGold(long characterId, float delta) {
        executor.execute(() -> {
            CharacterEntity character = characterDao.getCharacterSync(characterId);
            if (character != null) {
                character.currentGold = Math.max(0f, character.currentGold + delta);
                characterDao.update(character);
            }
        });
    }

    public void setGold(long characterId, float amount) {
        executor.execute(() -> {
            CharacterEntity character = characterDao.getCharacterSync(characterId);
            if (character != null) {
                character.currentGold = Math.max(0f, amount);
                characterDao.update(character);
            }
        });
    }

    // --- HP MANAGEMENT ---

    public void takeDamage(long characterId, int damage) {
        executor.execute(() -> {
            CharacterEntity c = characterDao.getCharacterSync(characterId);
            if (c == null) return;
            int remainingDamage = damage;
            if (c.temporaryHp > 0) {
                int tempAbsorb = Math.min(c.temporaryHp, remainingDamage);
                c.temporaryHp -= tempAbsorb;
                remainingDamage -= tempAbsorb;
            }
            c.currentHp = Math.max(0, c.currentHp - remainingDamage);
            characterDao.update(c);
        });
    }

    public void heal(long characterId, int amount) {
        executor.execute(() -> {
            CharacterEntity c = characterDao.getCharacterSync(characterId);
            if (c == null) return;
            int maxHp = engine.calculateMaxHp(characterId);
            c.currentHp = Math.min(maxHp, c.currentHp + amount);
            characterDao.update(c);
        });
    }

    public void setCurrentHp(long characterId, int hp) {
        executor.execute(() -> {
            CharacterEntity c = characterDao.getCharacterSync(characterId);
            if (c != null) {
                c.currentHp = hp;
                characterDao.update(c);
            }
        });
    }

    public void setTemporaryHp(long characterId, int tempAmount) {
        executor.execute(() -> {
            CharacterEntity c = characterDao.getCharacterSync(characterId);
            if (c == null) return;
            if (tempAmount > c.temporaryHp) {
                c.temporaryHp = tempAmount;
                characterDao.update(c);
            }
        });
    }

    public void setMaxHp(long characterId, int maxHp) {
        executor.execute(() -> {
            CharacterEntity c = characterDao.getCharacterSync(characterId);
            if (c == null) return;
            c.maxHp = maxHp;
            characterDao.update(c);
        });
    }

    // --- OTHER CHARACTER STATE ---

    public void setExhaustion(long characterId, int level) {
        executor.execute(() -> {
            CharacterEntity c = characterDao.getCharacterSync(characterId);
            if (c != null) {
                c.exhaustionLevel = level;
                characterDao.update(c);
            }
        });
    }

    public void setInspiration(long characterId, boolean has) {
        executor.execute(() -> {
            CharacterEntity c = characterDao.getCharacterSync(characterId);
            if (c != null) {
                c.hasInspiration = has;
                characterDao.update(c);
            }
        });
    }

    // --- INVENTORY MANAGEMENT ---

    public void addItem(InventoryItemEntity item) {
        executor.execute(() -> inventoryDao.insert(item));
    }

    public void removeItem(long itemId) {
        executor.execute(() -> inventoryDao.deleteItem(itemId));
    }

    public void toggleEquipItem(long itemId, boolean equipped, String slot) {
        executor.execute(() -> {
            InventoryItemEntity item = inventoryDao.getById(itemId);
            if (item != null) {
                item.isEquipped = equipped;
                item.slot = equipped ? slot : null;
                inventoryDao.update(item);
            }
        });
    }

    public void buyItem(long characterId, float cost, InventoryItemEntity item) {
        executor.execute(() -> {
            pcDb.runInTransaction(() -> {
                CharacterEntity character = characterDao.getCharacterSync(characterId);
                if (character != null && character.currentGold >= cost) {
                    character.currentGold -= cost;
                    characterDao.update(character);
                    item.characterId = characterId;
                    inventoryDao.insert(item);
                }
            });
        });
    }

    public void deleteCharacter(long characterId) {
        executor.execute(() -> characterDao.deleteCharacter(characterId));
    }

    // --- DATA ACCESS ---

    public LiveData<CharacterEntity> getCharacter(long id) {
        return characterDao.getCharacter(id);
    }

    public CharacterEntity getCharacterSync(long id) {
        return characterDao.getCharacterSync(id);
    }

    public LiveData<CharacterWithRelations> getCharacterWithRelations(long id) {
        return characterDao.getCharacterWithRelations(id);
    }

    public List<CharacterClassAssignmentEntity> getClassAssignments(long characterId) {
        return pcDb.classAssignmentDao().getByCharacterId(characterId);
    }

    public CharacterAttributesEntity getAttributes(long characterId) {
        return pcDb.characterAttributesDao().getByCharacterId(characterId);
    }

    public List<CharacterLanguageEntity> getLanguages(long characterId) {
        return pcDb.languageDao().getByCharacterId(characterId);
    }

    public List<CharacterTraitEntity> getTraits(long characterId) {
        return pcDb.traitDao().getByCharacterId(characterId);
    }

    public List<InventoryItemEntity> getInventory(long characterId) {
        return pcDb.inventoryItemDao().getByCharacterId(characterId);
    }

    public List<CharacterSpellEntity> getSpells(long characterId) {
        return pcDb.spellDao().getByCharacterId(characterId);
    }

    public CharacterEngine getEngine() {
        return engine;
    }

    public Open5eDatabase getOpen5eDb() {
        return open5eDb;
    }
}
