package com.murkfeatherstudio.questroll.feature_character.utils;

import static org.junit.Assert.*;
import org.junit.Test;
import java.util.List;
import com.murkfeatherstudio.questroll.core.models.character.CharacterCreationDTO;

public class BenefitParserTest {

    @Test
    public void parseBenefits_extractsGoldFromEquipment() {
        String json = "[{\"type\": \"equipment\", \"desc\": \"A set of common clothes and 15 gp\"}]";
        BenefitParser.ParsedBenefits result = BenefitParser.parseBenefits(json);
        assertEquals(15, result.gold);
        assertEquals("A set of common clothes and 15 gp", result.equipmentDescription);
    }

    @Test
    public void parseBenefits_extractsSkillProficiencies() {
        String json = "[{\"type\": \"skill_proficiency\", \"desc\": \"Athletics, Insight\"}]";
        BenefitParser.ParsedBenefits result = BenefitParser.parseBenefits(json);
        assertTrue(result.skillProficiencies.contains("Athletics"));
        assertTrue(result.skillProficiencies.contains("Insight"));
        assertEquals(2, result.skillProficiencies.size());
    }

    @Test
    public void parseBenefits_extractsLanguages() {
        String json = "[{\"type\": \"language\", \"desc\": \"Common, Elvish\"}]";
        BenefitParser.ParsedBenefits result = BenefitParser.parseBenefits(json);
        assertTrue(result.fixedLanguages.contains("Common"));
        assertTrue(result.fixedLanguages.contains("Elvish"));
    }

    @Test
    public void parseBenefits_handlesLanguageChoices() {
        String json = "[{\"type\": \"language\", \"desc\": \"Two of your choice\"}]";
        BenefitParser.ParsedBenefits result = BenefitParser.parseBenefits(json);
        assertEquals(2, result.languageChoices);
    }

    @Test
    public void parseClassEquipment_cleansUpTextAndSplitsItems() {
        String raw = "You start with the following equipment, in addition to the equipment granted by your background: (a) a longsword or (b) any martial weapon, (a) a shield or (b) any martial weapon";
        List<CharacterCreationDTO.InventoryItemDTO> items = BenefitParser.parseClassEquipment(raw);
        
        // The parser splits by comma and removes prefixes
        assertFalse(items.isEmpty());
        assertTrue(items.get(0).customName.contains("longsword"));
    }
}
