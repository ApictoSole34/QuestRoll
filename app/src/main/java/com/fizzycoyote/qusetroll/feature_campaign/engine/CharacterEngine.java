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

/**
 * A utility engine for performing character-related calculations within the campaign feature.
 * <p>
 * This version of the engine provides stateless calculation methods for skills, saving throws,
 * hit points, and other derived statistics using D&D 5e rules.
 * </p>
 */
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

    /**
     * Calculates the total bonus for all standard skills.
     *
     * @param attributes            The character's attribute scores.
     * @param skillProficiencyKeys  Keys of skills the character is proficient in.
     * @param totalLevel            The character's total level.
     * @return A map of skill keys to their total calculated bonuses.
     */
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

    /**
     * Calculates saving throw bonuses for all six abilities.
     *
     * @param attributes   The character's attribute scores.
     * @param savingThrows List of saving throw proficiency records.
     * @param totalLevel   The character's total level.
     * @return A map of ability keys (e.g., "STR") to their saving throw bonuses.
     */
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

    /**
     * Calculates the proficiency bonus for a given level.
     *
     * @param totalLevel The total character level.
     * @return The calculated proficiency bonus.
     */
    public static int getProficiencyBonus(int totalLevel) {
        return 1 + (int) Math.ceil(totalLevel / 4.0);
    }

    /**
     * Calculates the ability modifier from an ability score.
     *
     * @param attributes The character's attribute scores.
     * @param abilityKey The ability key (e.g., "STR").
     * @return The calculated modifier (e.g., 14 -> +2).
     */
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

    /**
     * Sums the levels from a list of class assignments.
     *
     * @param classAssignments The character's class assignments.
     * @return The total level.
     */
    public int getTotalLevel(List<CharacterClassAssignmentEntity> classAssignments) {
        if (classAssignments == null) return 0;
        int sum = 0;
        for (CharacterClassAssignmentEntity ca : classAssignments) {
            sum += ca.level;
        }
        return sum;
    }

    /**
     * Calculates maximum HP based on level and Constitution modifier.
     * <p>
     * Note: This is a simplified calculation (Level * 8 + Level * ConMod).
     * </p>
     *
     * @param attributes The character's attribute scores.
     * @param totalLevel The character's total level.
     * @return The maximum hit points.
     */
    public int calculateMaxHp(CharacterAttributesEntity attributes, int totalLevel) {
        int conMod = getAbilityModifier(attributes, "CON");
        return (totalLevel * 8) + (totalLevel * conMod);
    }

    /**
     * Gets the initiative bonus.
     *
     * @param attributes The character's attribute scores.
     * @return The Dexterity modifier.
     */
    public int getInitiative(CharacterAttributesEntity attributes) {
        return getAbilityModifier(attributes, "DEX");
    }

    /**
     * Calculates base AC.
     *
     * @param attributes The character's attribute scores.
     * @return 10 + Dexterity modifier.
     */
    public int getArmorClass(CharacterAttributesEntity attributes) {
        return 10 + getAbilityModifier(attributes, "DEX");
    }
}
