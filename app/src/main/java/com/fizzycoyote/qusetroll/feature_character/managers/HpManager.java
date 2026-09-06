package com.fizzycoyote.qusetroll.feature_character.managers;

import com.fizzycoyote.qusetroll.core.local_database.Open5eDatabase;
import com.fizzycoyote.qusetroll.core.local_database.PlayerCharacterDatabase;
import com.fizzycoyote.qusetroll.core.models.character.CharacterAttributesEntity;
import com.fizzycoyote.qusetroll.core.models.character.CharacterClassAssignmentEntity;
import com.fizzycoyote.qusetroll.core.models.character.CharacterEntity;
import com.fizzycoyote.qusetroll.core.models.open5e.character_class.CharacterClassEntity;

import java.util.List;

/**
 * Manager responsible for handling character health-related operations.
 * <p>
 * This includes calculating maximum HP based on class levels and modifiers,
 * applying damage (accounting for temporary HP), healing, and managing
 * temporary hit points.
 * </p>
 */
public class HpManager {
    private final PlayerCharacterDatabase pcDb;
    private final Open5eDatabase open5eDb;

    public HpManager(PlayerCharacterDatabase pcDb, Open5eDatabase open5eDb) {
        this.pcDb = pcDb;
        this.open5eDb = open5eDb;
    }

    /**
     * Calculates the maximum Hit Points for a character based on their class assignments
     * and Constitution modifier.
     *
     * @param characterId The unique ID of the character.
     * @return The calculated maximum Hit Points.
     */
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

    /**
     * Deducts damage from a character's current health.
     * <p>
     * Temporary Hit Points are consumed first. Remaining damage reduces
     * the current HP, but not below zero.
     * </p>
     *
     * @param characterId The unique ID of the character.
     * @param damage      The amount of damage to apply.
     */
    public void takeDamage(long characterId, int damage) {
        CharacterEntity c = pcDb.characterDao().getCharacterSync(characterId);
        if (c == null) return;
        int remainingDamage = damage;
        if (c.temporaryHp > 0) {
            int tempAbsorb = Math.min(c.temporaryHp, remainingDamage);
            c.temporaryHp -= tempAbsorb;
            remainingDamage -= tempAbsorb;
        }
        c.currentHp = Math.max(0, c.currentHp - remainingDamage);
        pcDb.characterDao().update(c);
    }

    /**
     * Increases a character's current health.
     * <p>
     * Current HP is increased by the specified amount, up to the maximum HP limit.
     * </p>
     *
     * @param characterId The unique ID of the character.
     * @param amount      The amount of healing to apply.
     */
    public void heal(long characterId, int amount) {
        CharacterEntity c = pcDb.characterDao().getCharacterSync(characterId);
        if (c == null) return;
        int maxHp = calculateMaxHp(characterId);
        c.currentHp = Math.min(maxHp, c.currentHp + amount);
        pcDb.characterDao().update(c);
    }

    /**
     * Adds temporary hit points to a character.
     * <p>
     * According to 5e rules, temporary HP does not stack; the highest value persists.
     * </p>
     *
     * @param characterId The unique ID of the character.
     * @param tempAmount  The amount of temporary HP to set.
     */
    public void addTempHp(long characterId, int tempAmount) {
        CharacterEntity c = pcDb.characterDao().getCharacterSync(characterId);
        if (c == null) return;
        if (tempAmount > c.temporaryHp) {
            c.temporaryHp = tempAmount;
            pcDb.characterDao().update(c);
        }
    }
}
