package com.murkfeatherstudio.questroll.feature_character.managers;

import com.murkfeatherstudio.questroll.core.local_database.Open5eDatabase;
import com.murkfeatherstudio.questroll.core.local_database.PlayerCharacterDatabase;
import com.murkfeatherstudio.questroll.core.models.character.CharacterAttributesEntity;
import com.murkfeatherstudio.questroll.core.models.character.CharacterClassAssignmentEntity;
import com.murkfeatherstudio.questroll.core.models.character.CharacterEntity;
import com.murkfeatherstudio.questroll.core.models.open5e.character_class.CharacterClassEntity;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Manager responsible for handling character health-related operations.
 */
public class HpManager {
    private final PlayerCharacterDatabase pcDb;
    private final Open5eDatabase open5eDb;

    public HpManager(PlayerCharacterDatabase pcDb, Open5eDatabase open5eDb) {
        this.pcDb = pcDb;
        this.open5eDb = open5eDb;
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
                if (lvl == 1) {
                    totalHp += parseHpString(classEntity.hitPointsAt1stLevel, conMod, true);
                } else {
                    totalHp += parseHpString(classEntity.hitPointsAtHigherLevels, conMod, false);
                }
            }
        }
        return totalHp;
    }

    /**
     * Parses HP strings from Open5e. 
     * Delegating to logic similar to CharacterEngine for consistency.
     */
    private int parseHpString(String hpStr, int conMod, boolean isFirstLevel) {
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

    public void heal(long characterId, int amount) {
        CharacterEntity c = pcDb.characterDao().getCharacterSync(characterId);
        if (c == null) return;
        int maxHp = calculateMaxHp(characterId);
        c.currentHp = Math.min(maxHp, c.currentHp + amount);
        pcDb.characterDao().update(c);
    }

    public void addTempHp(long characterId, int tempAmount) {
        CharacterEntity c = pcDb.characterDao().getCharacterSync(characterId);
        if (c == null) return;
        if (tempAmount > c.temporaryHp) {
            c.temporaryHp = tempAmount;
            pcDb.characterDao().update(c);
        }
    }
}
