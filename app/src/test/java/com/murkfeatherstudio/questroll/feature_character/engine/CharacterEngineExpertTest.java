package com.murkfeatherstudio.questroll.feature_character.engine;

import static org.junit.Assert.*;
import org.junit.Test;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import com.murkfeatherstudio.questroll.feature_campaign.engine.CharacterEngine;
import com.murkfeatherstudio.questroll.core.models.character.CharacterAttributesEntity;

public class CharacterEngineExpertTest {

    private final CharacterEngine engine = new CharacterEngine();

    @Test
    public void calculateSkillBonus_isCorrect() {
        CharacterAttributesEntity attrs = new CharacterAttributesEntity();
        attrs.dexterity = 16;
        attrs.dexterityMod = 3;
        
        // Using actual production engine
        List<String> proficiencies = Arrays.asList("stealth");
        Map<String, Integer> bonuses = engine.getSkillBonuses(attrs, proficiencies, 1);
        
        assertEquals("Stealth should be 3 (DEX) + 2 (Prof) = 5", 
                Integer.valueOf(5), bonuses.get("stealth"));
        assertEquals("Acrobatics (not proficient) should be 3", 
                Integer.valueOf(3), bonuses.get("acrobatics"));
    }

    @Test
    public void passivePerception_isCorrect() {
        CharacterAttributesEntity attrs = new CharacterAttributesEntity();
        attrs.wisdom = 14;
        attrs.wisdomMod = 2;
        
        // Proficiency in Perception
        List<String> proficiencies = Arrays.asList("perception");
        int totalLevel = 1; // +2 bonus
        
        Map<String, Integer> bonuses = engine.getSkillBonuses(attrs, proficiencies, totalLevel);
        int perceptionBonus = bonuses.getOrDefault("perception", 0);
        
        int passivePerception = 10 + perceptionBonus;
        assertEquals(14, passivePerception);
    }
}
