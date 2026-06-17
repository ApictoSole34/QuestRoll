package com.fizzycoyote.qusetroll.feature_campaign.engine;

import com.fizzycoyote.qusetroll.core.models.character.CharacterAttributesEntity;
import com.fizzycoyote.qusetroll.core.models.character.CharacterClassAssignmentEntity;
import com.fizzycoyote.qusetroll.core.models.character.CharacterSavingThrowEntity;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class CharacterEngine {

    private static final Map<String, String> SKILL_ABILITY_MAP = new HashMap<>();

    static {
        // Standard 5e skills
        SKILL_ABILITY_MAP.put("acrobatics", "DEX");
        SKILL_ABILITY_MAP.put("animal_handling", "WIS");
        SKILL_ABILITY_MAP.put("arcana", "INT");
        SKILL_ABILITY_MAP.put("athletics", "STR");
        SKILL_ABILITY_MAP.put("deception", "CHA");
        SKILL_ABILITY_MAP.put("history", "INT");
        SKILL_ABILITY_MAP.put("insight", "WIS");
        SKILL_ABILITY_MAP.put("intimidation", "CHA");
        SKILL_ABILITY_MAP.put("investigation", "INT");
        SKILL_ABILITY_MAP.put("medicine", "WIS");
        SKILL_ABILITY_MAP.put("nature", "INT");
        SKILL_ABILITY_MAP.put("perception", "WIS");
        SKILL_ABILITY_MAP.put("performance", "CHA");
        SKILL_ABILITY_MAP.put("persuasion", "CHA");
        SKILL_ABILITY_MAP.put("religion", "INT");
        SKILL_ABILITY_MAP.put("sleight_of_hand", "DEX");
        SKILL_ABILITY_MAP.put("stealth", "DEX");
        SKILL_ABILITY_MAP.put("survival", "WIS");

        SKILL_ABILITY_MAP.put("custom_skill", "INT");
    }

    public Map<String, Integer> getSkillBonuses(CharacterAttributesEntity attributes,
                                                List<String> skillProficiencyKeys,
                                                int totalLevel) {
        Map<String, Integer> bonuses = new HashMap<>();
        if (attributes == null) return bonuses;

        int profBonus = getProficiencyBonus(totalLevel);

        for (Map.Entry<String, String> entry : SKILL_ABILITY_MAP.entrySet()) {
            String skillKey = entry.getKey();
            String abilityKey = entry.getValue();
            int abilityMod = getAbilityModifier(attributes, abilityKey);
            int bonus = abilityMod;
            if (skillProficiencyKeys != null && skillProficiencyKeys.contains(skillKey)) {
                bonus += profBonus;
            }
            bonuses.put(skillKey, bonus);
        }
        return bonuses;
    }

    public Map<String, Integer> getSavingThrowBonuses(CharacterAttributesEntity attributes,
                                                      List<CharacterSavingThrowEntity> savingThrows,
                                                      int totalLevel) {
        Map<String, Integer> bonuses = new HashMap<>();
        if (attributes == null) return bonuses;
        int profBonus = getProficiencyBonus(totalLevel);
        Set<String> proficient = (savingThrows == null) ? new HashSet<>() :
                savingThrows.stream().filter(st -> st.isProficient)
                        .map(st -> st.abilityKey).collect(Collectors.toSet());
        String[] abilities = {"STR", "DEX", "CON", "INT", "WIS", "CHA"};
        for (String ab : abilities) {
            int mod = getAbilityModifier(attributes, ab);
            if (proficient.contains(ab)) mod += profBonus;
            bonuses.put(ab, mod);
        }
        return bonuses;
    }

    public static int getProficiencyBonus(int totalLevel) {
        return 1 + (int) Math.ceil(totalLevel / 4.0);
    }

    public int getAbilityModifier(CharacterAttributesEntity attributes, String abilityKey) {
        if (attributes == null) return 0;
        int score = 0;
        switch (abilityKey) {
            case "STR": score = attributes.strength; break;
            case "DEX": score = attributes.dexterity; break;
            case "CON": score = attributes.constitution; break;
            case "INT": score = attributes.intelligence; break;
            case "WIS": score = attributes.wisdom; break;
            case "CHA": score = attributes.charisma; break;
            default: return 0;
        }
        return (score - 10) / 2;
    }

    public int getTotalLevel(List<CharacterClassAssignmentEntity> classAssignments) {
        if (classAssignments == null) return 0;
        int sum = 0;
        for (CharacterClassAssignmentEntity ca : classAssignments) {
            sum += ca.level;
        }
        return sum;
    }

    public int calculateMaxHp(CharacterAttributesEntity attributes, int totalLevel) {
        int conMod = getAbilityModifier(attributes, "CON");
        return (totalLevel * 8) + (totalLevel * conMod);
    }

    public int getInitiative(CharacterAttributesEntity attributes) {
        return getAbilityModifier(attributes, "DEX");
    }

    public int getArmorClass(CharacterAttributesEntity attributes) {
        return 10 + getAbilityModifier(attributes, "DEX");
    }
}