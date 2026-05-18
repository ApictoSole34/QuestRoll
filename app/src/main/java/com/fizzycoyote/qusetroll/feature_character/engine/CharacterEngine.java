package com.fizzycoyote.qusetroll.feature_character.engine;

import android.content.Context;

import com.fizzycoyote.qusetroll.core.local_database.Open5eDatabase;
import com.fizzycoyote.qusetroll.core.local_database.PlayerCharacterDatabase;
import com.fizzycoyote.qusetroll.core.models.character.CharacterAttributesEntity;
import com.fizzycoyote.qusetroll.core.models.character.CharacterClassAssignmentEntity;
import com.fizzycoyote.qusetroll.core.models.character.CharacterEntity;
import com.fizzycoyote.qusetroll.core.models.character.CharacterTraitEntity;
import com.fizzycoyote.qusetroll.core.models.character.InventoryItemEntity;
import com.fizzycoyote.qusetroll.core.models.open5e.ability.skill.SkillEntity;
import com.fizzycoyote.qusetroll.core.models.open5e.character_class.CharacterClassEntity;
import com.fizzycoyote.qusetroll.core.models.open5e.character_class.feature.FeatureEntity;
import com.fizzycoyote.qusetroll.core.models.open5e.character_class.saving_throw.SavingThrowEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CharacterEngine {

    private final Open5eDatabase open5eDb;
    private final PlayerCharacterDatabase pcDb;

    public CharacterEngine(Context context) {
        this.open5eDb = Open5eDatabase.getInstance(context);
        this.pcDb = PlayerCharacterDatabase.getInstance(context);
    }

    // Proficiency bonus as per D&D 5e (level 1-4: +2, 5-8: +3, etc.)
    public int getProficiencyBonus(int totalLevel) {
        return 2 + (totalLevel - 1) / 4;
    }

    public int getTotalLevel(long characterId) {
        List<CharacterClassAssignmentEntity> assignments = pcDb.classAssignmentDao().getByCharacterId(characterId);
        int total = 0;
        for (CharacterClassAssignmentEntity ca : assignments) total += ca.level;
        return total;
    }

    public int getInitiative(long characterId) {
        CharacterAttributesEntity attrs = pcDb.attributesDao().getByCharacterId(characterId);
        return (attrs != null) ? attrs.dexterityMod : 0;
    }

    public int getArmorClass(long characterId) {
        CharacterAttributesEntity attrs = pcDb.attributesDao().getByCharacterId(characterId);
        int dexMod = (attrs != null) ? attrs.dexterityMod : 0;
        int baseAc = 10 + dexMod;
        List<InventoryItemEntity> inventory = pcDb.inventoryItemDao().getByCharacterId(characterId);
        for (InventoryItemEntity item : inventory) {
            if (item.isEquipped && item.itemKey != null && "armor".equals(item.slot)) {
                baseAc += 2;
            }
        }
        return baseAc;
    }

    public int calculateMaxHp(long characterId) {
        CharacterEntity character = pcDb.characterDao().getCharacterSync(characterId);
        if (character == null) return 0;
        List<CharacterClassAssignmentEntity> assignments = pcDb.classAssignmentDao().getByCharacterId(characterId);
        CharacterAttributesEntity attrs = pcDb.attributesDao().getByCharacterId(characterId);
        int conMod = (attrs != null) ? attrs.constitutionMod : 0;
        int totalHp = 0;
        for (CharacterClassAssignmentEntity ca : assignments) {
            CharacterClassEntity classEntity = open5eDb.characterClassDao().getClassByKeySync(ca.classKey);
            if (classEntity == null) continue;
            for (int lvl = 1; lvl <= ca.level; lvl++) {
                if (lvl == 1) {
                    totalHp += parseHpString(classEntity.hitPointsAt1stLevel, conMod);
                } else {
                    totalHp += parseHpString(classEntity.hitPointsAtHigherLevels, conMod);
                }
            }
        }
        return totalHp;
    }

    private int parseHpString(String hpStr, int conMod) {
        if (hpStr == null) return 0;
        try {
            if (hpStr.contains("+")) {
                String[] parts = hpStr.split("\\+");
                int base = Integer.parseInt(parts[0].trim());
                return base + conMod;
            } else {
                return Integer.parseInt(hpStr.trim()) + conMod;
            }
        } catch (NumberFormatException e) {
            return conMod;
        }
    }

    public List<String> getSavingThrowProficiencies(long characterId) {
        List<CharacterClassAssignmentEntity> assignments = pcDb.classAssignmentDao().getByCharacterId(characterId);
        List<String> proficientSaves = new ArrayList<>();
        for (CharacterClassAssignmentEntity ca : assignments) {
            List<SavingThrowEntity> saves = open5eDb.savingThrowDao().getSavingThrowsForClassSync(ca.classKey);
            for (SavingThrowEntity save : saves) {
                if (!proficientSaves.contains(save.abilityKey)) {
                    proficientSaves.add(save.abilityKey);
                }
            }
        }
        return proficientSaves;
    }

    public Map<String, Integer> getSkillBonuses(long characterId) {
        Map<String, Integer> bonuses = new HashMap<>();
        CharacterAttributesEntity attrs = pcDb.attributesDao().getByCharacterId(characterId);
        if (attrs == null) return bonuses;

        List<SkillEntity> allSkills = open5eDb.skillDao().getAllSync();
        List<String> proficientSkills = pcDb.characterSkillProficiencyDao().getSkillKeysForCharacter(characterId);
        int profBonus = getProficiencyBonus(getTotalLevel(characterId));

        for (SkillEntity skill : allSkills) {
            if (isA5eSkill(skill.key)) continue;
            int abilityMod = getAbilityMod(attrs, skill.abilityKey);
            int bonus = abilityMod;
            if (proficientSkills.contains(skill.key)) {
                bonus += profBonus;
            }
            bonuses.put(skill.key, bonus);
        }
        return bonuses;
    }

    private boolean isA5eSkill(String skillKey) {
        return skillKey != null && (skillKey.startsWith("a5e_") || skillKey.contains("a5e-ag"));
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

    @Deprecated
    private List<String> getProficientSkills(long characterId) {
        List<String> skills = new ArrayList<>();
        List<CharacterTraitEntity> traits = pcDb.traitDao().getByCharacterId(characterId);
        for (CharacterTraitEntity t : traits) {
            if (t.name != null && t.name.toLowerCase().contains("skill proficiency")) {
                if (t.description != null && !t.description.isEmpty()) {
                    skills.add(t.description);
                }
            }
        }
        return skills;
    }

    public List<FeatureEntity> getAvailableClassFeatures(long characterId, String classKey, int currentLevel) {
        List<FeatureEntity> allFeatures = open5eDb.featureDao().getFeaturesForClassSync(classKey);
        List<FeatureEntity> available = new ArrayList<>();
        for (FeatureEntity f : allFeatures) {
            if (f.gainedAt != null) {
                for (var gained : f.gainedAt) {
                    if (gained.level <= currentLevel) {
                        available.add(f);
                        break;
                    }
                }
            }
        }
        return available;
    }

    public List<CharacterTraitEntity> getAllTraits(long characterId) {
        return pcDb.traitDao().getByCharacterId(characterId);
    }
}
