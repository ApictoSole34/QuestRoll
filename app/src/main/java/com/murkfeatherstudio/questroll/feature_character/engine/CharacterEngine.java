package com.murkfeatherstudio.questroll.feature_character.engine;

import android.content.Context;

import com.murkfeatherstudio.questroll.core.local_database.Open5eDatabase;
import com.murkfeatherstudio.questroll.core.local_database.PlayerCharacterDatabase;
import com.murkfeatherstudio.questroll.core.models.character.CharacterAttributesEntity;
import com.murkfeatherstudio.questroll.core.models.character.CharacterClassAssignmentEntity;
import com.murkfeatherstudio.questroll.core.models.character.CharacterEntity;
import com.murkfeatherstudio.questroll.core.models.character.InventoryItemEntity;
import com.murkfeatherstudio.questroll.core.models.open5e.ability.skill.SkillEntity;
import com.murkfeatherstudio.questroll.core.models.open5e.character_class.CharacterClassEntity;
import com.murkfeatherstudio.questroll.core.models.open5e.character_class.feature.FeatureEntity;
import com.murkfeatherstudio.questroll.core.models.open5e.character_class.saving_throw.SavingThrowEntity;
import com.murkfeatherstudio.questroll.core.models.open5e.item.ItemDto;
import com.murkfeatherstudio.questroll.core.models.open5e.item.ItemEntity;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The core game logic engine for character-related calculations.
 */
public class CharacterEngine {

    /** Inventory slot key used for body armor. */
    private static final String SLOT_BODY = "body";
    /** Inventory slot key used for a shield, worn in the off hand. */
    private static final String SLOT_OFF_HAND = "off_hand";

    private static final int[] XP_THRESHOLDS = {
            0, 300, 900, 2700, 6500, 14000, 23000, 34000, 48000, 64000,
            85000, 100000, 120000, 140000, 165000, 195000, 225000, 265000, 305000, 355000
    };

    private final Open5eDatabase open5eDb;
    private final PlayerCharacterDatabase pcDb;
    private final Gson gson = new Gson();

    public CharacterEngine(Context context) {
        this(Open5eDatabase.getInstance(context), PlayerCharacterDatabase.getInstance(context));
    }

    public CharacterEngine(Open5eDatabase open5eDb, PlayerCharacterDatabase pcDb) {
        this.open5eDb = open5eDb;
        this.pcDb = pcDb;
    }

    // --- STATIC STATELESS CALCULATION METHODS ---

    /**
     * Calculates the proficiency bonus based on total level.
     */
    public static int getProficiencyBonus(int totalLevel) {
        return 2 + (Math.max(1, totalLevel) - 1) / 4;
    }

    /**
     * Calculates the ability modifier from a score. Rounds down.
     */
    public static int getAbilityModifier(int score) {
        return Math.floorDiv(score - 10, 2);
    }

    /**
     * Determines character level based on total experience points.
     */
    public static int getLevelFromXp(int xp) {
        for (int i = XP_THRESHOLDS.length - 1; i >= 0; i--) {
            if (xp >= XP_THRESHOLDS[i]) {
                return i + 1;
            }
        }
        return 1;
    }

    /**
     * Calculates passive perception: 10 + WIS mod + (Proficiency if applicable).
     */
    public static int calculatePassivePerception(int wisMod, int profBonus, boolean isProficient) {
        return 10 + wisMod + (isProficient ? profBonus : 0);
    }

    /**
     * Calculates Spell Save DC: 8 + Proficiency + Casting Ability Mod.
     */
    public static int calculateSpellSaveDc(int profBonus, int abilityMod) {
        return 8 + profBonus + abilityMod;
    }

    /**
     * Calculates Spell Attack Bonus: Proficiency + Casting Ability Mod.
     */
    public static int calculateSpellAttackBonus(int profBonus, int abilityMod) {
        return profBonus + abilityMod;
    }

    /**
     * Parses HP strings from Open5e (e.g., "1d8 (or 5) + CON").
     */
    public static int parseHpString(String hpStr, int conMod, boolean isFirstLevel) {
        if (hpStr == null || hpStr.isEmpty()) return conMod;

        Pattern orPattern = Pattern.compile("or (\\d+)");
        Matcher orMatcher = orPattern.matcher(hpStr);
        if (orMatcher.find()) {
            return Integer.parseInt(orMatcher.group(1)) + conMod;
        }

        Pattern diePattern = Pattern.compile("(\\d+)d(\\d+)");
        Matcher dieMatcher = diePattern.matcher(hpStr);
        if (dieMatcher.find()) {
            int sides = Integer.parseInt(dieMatcher.group(2));
            if (isFirstLevel) {
                return sides + conMod;
            } else {
                return (sides / 2 + 1) + conMod;
            }
        }

        try {
            String firstPart = hpStr.split("[^0-9]")[0];
            if (!firstPart.isEmpty()) {
                return Integer.parseInt(firstPart) + conMod;
            }
        } catch (Exception ignored) {}

        return conMod;
    }

    /**
     * Parses a hit die string (e.g., "d8" or "1d10") and returns the number of sides.
     */
    public static int parseHitDie(String hitDice) {
        if (hitDice == null || hitDice.isEmpty()) return 8;
        String s = hitDice.toLowerCase().trim();
        int d = s.lastIndexOf('d');
        if (d < 0) {
            try { return Integer.parseInt(s); }
            catch (NumberFormatException e) { return 8; }
        }
        try {
            return Integer.parseInt(s.substring(d + 1).trim());
        } catch (NumberFormatException e) {
            return 8;
        }
    }

    /**
     * Filters features available at or below a specific level.
     */
    public static List<FeatureEntity> filterFeaturesByLevel(List<FeatureEntity> all, int level) {
        List<FeatureEntity> available = new ArrayList<>();
        if (all == null) return available;
        for (FeatureEntity f : all) {
            if (f.gainedAt != null) {
                for (var gained : f.gainedAt) {
                    if (gained.level <= level) {
                        available.add(f);
                        break;
                    }
                }
            }
        }
        return available;
    }

    /**
     * Calculates current gold after a purchase. Pure logic helper.
     */
    public static float calculatePurchase(float currentGold, float cost) {
        if (currentGold >= cost) {
            return currentGold - cost;
        }
        return currentGold;
    }

    // --- INSTANCE METHODS (DATABASE DEPENDENT) ---

    public int getTotalLevel(long characterId) {
        List<CharacterClassAssignmentEntity> assignments = pcDb.classAssignmentDao().getByCharacterId(characterId);
        int total = 0;
        for (CharacterClassAssignmentEntity ca : assignments) total += ca.level;
        return total;
    }

    public int getInitiative(long characterId) {
        CharacterAttributesEntity attrs = pcDb.characterAttributesDao().getByCharacterId(characterId);
        return (attrs != null) ? attrs.dexterityMod : 0;
    }

    public int getArmorClass(long characterId) {
        CharacterAttributesEntity attrs = pcDb.characterAttributesDao().getByCharacterId(characterId);
        int dexMod = (attrs != null) ? attrs.dexterityMod : 0;
        List<InventoryItemEntity> inventory = pcDb.inventoryItemDao().getByCharacterId(characterId);

        int bodyAc = 10 + dexMod;
        int shieldBonus = 0;

        for (InventoryItemEntity invItem : inventory) {
            if (!invItem.isEquipped || invItem.itemKey == null || invItem.slot == null) continue;
            ItemDto.ArmorEmbedDto armor = getArmorData(invItem.itemKey);
            if (armor == null) continue;

            if (SLOT_BODY.equals(invItem.slot)) {
                int dexContribution = armor.acAddDexmod ?
                        ((armor.acCapDexmod != null) ? Math.min(dexMod, armor.acCapDexmod) : dexMod) : 0;
                bodyAc = armor.acBase + dexContribution;
            } else if (SLOT_OFF_HAND.equals(invItem.slot)) {
                shieldBonus = armor.acBase;
            }
        }
        return bodyAc + shieldBonus;
    }

    private ItemDto.ArmorEmbedDto getArmorData(String itemKey) {
        ItemEntity item = open5eDb.itemDao().getByKeySync(itemKey);
        if (item == null || item.armorJson == null || item.armorJson.isEmpty()) return null;
        try {
            return gson.fromJson(item.armorJson, ItemDto.ArmorEmbedDto.class);
        } catch (Exception e) {
            return null;
        }
    }

    public int calculateMaxHp(long characterId) {
        CharacterEntity character = pcDb.characterDao().getCharacterSync(characterId);
        if (character == null) return 0;
        List<CharacterClassAssignmentEntity> assignments = pcDb.classAssignmentDao().getByCharacterId(characterId);
        CharacterAttributesEntity attrs = pcDb.characterAttributesDao().getByCharacterId(characterId);
        int conMod = (attrs != null) ? attrs.constitutionMod : 0;
        int totalHp = 0;
        for (CharacterClassAssignmentEntity ca : assignments) {
            CharacterClassEntity classEntity = open5eDb.characterClassDao().getClassByKeySync(ca.classKey);
            if (classEntity == null) continue;
            for (int lvl = 1; lvl <= ca.level; lvl++) {
                totalHp += parseHpString(lvl == 1 ? classEntity.hitPointsAt1stLevel : classEntity.hitPointsAtHigherLevels, conMod, lvl == 1);
            }
        }
        return totalHp;
    }

    public List<String> getSavingThrowProficiencies(long characterId) {
        List<CharacterClassAssignmentEntity> assignments = pcDb.classAssignmentDao().getByCharacterId(characterId);
        List<String> proficientSaves = new ArrayList<>();
        for (CharacterClassAssignmentEntity ca : assignments) {
            List<SavingThrowEntity> saves = open5eDb.savingThrowDao().getSavingThrowsForClassSync(ca.classKey);
            for (SavingThrowEntity save : saves) {
                if (!proficientSaves.contains(save.abilityKey)) proficientSaves.add(save.abilityKey);
            }
        }
        return proficientSaves;
    }

    public Map<String, Integer> getSkillBonuses(long characterId) {
        Map<String, Integer> bonuses = new HashMap<>();
        CharacterAttributesEntity attrs = pcDb.characterAttributesDao().getByCharacterId(characterId);
        if (attrs == null) return bonuses;

        List<SkillEntity> allSkills = open5eDb.skillDao().getAllSync();
        List<String> proficientSkills = pcDb.characterSkillProficiencyDao().getSkillKeysForCharacter(characterId);
        int profBonus = getProficiencyBonus(getTotalLevel(characterId));

        for (SkillEntity skill : allSkills) {
            if (skill.key != null && (skill.key.startsWith("a5e_") || skill.key.contains("a5e-ag"))) continue;
            int abilityMod = getAbilityMod(attrs, skill.abilityKey);
            bonuses.put(skill.key, abilityMod + (proficientSkills.contains(skill.key) ? profBonus : 0));
        }
        return bonuses;
    }

    private int getAbilityMod(CharacterAttributesEntity attrs, String abilityKey) {
        if (abilityKey == null) return 0;
        switch (abilityKey.toUpperCase()) {
            case "STR": return attrs.strengthMod;
            case "DEX": return attrs.dexterityMod;
            case "CON": return attrs.constitutionMod;
            case "INT": return attrs.intelligenceMod;
            case "WIS": return attrs.wisdomMod;
            case "CHA": return attrs.charismaMod;
            default: return 0;
        }
    }
}
