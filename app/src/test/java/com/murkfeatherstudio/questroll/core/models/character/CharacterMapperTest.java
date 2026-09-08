package com.murkfeatherstudio.questroll.core.models.character;

import static org.junit.Assert.assertEquals;
import org.junit.Test;
import java.util.HashMap;
import java.util.ArrayList;

public class CharacterMapperTest {

    @Test
    public void toAttributesEntity_calculatesModifiersCorrectly() {
        CharacterCreationDTO dto = new CharacterCreationDTO();
        dto.attributes = new HashMap<>();
        
        // D&D 5e: Mod = floor((score - 10) / 2)
        dto.attributes.put("STR", 15); // +2
        dto.attributes.put("DEX", 10); // +0
        dto.attributes.put("CON", 8);  // -1
        dto.attributes.put("INT", 7);  // -2
        dto.attributes.put("WIS", 18); // +4
        dto.attributes.put("CHA", 3);  // -4

        CharacterAttributesEntity attrs = CharacterMapper.toAttributesEntity(1L, dto);

        assertEquals("Score 15 should give +2", 2, attrs.strengthMod);
        assertEquals("Score 10 should give +0", 0, attrs.dexterityMod);
        assertEquals("Score 8 should give -1", -1, attrs.constitutionMod);
        assertEquals("Score 7 should give -2", -2, attrs.intelligenceMod);
        assertEquals("Score 18 should give +4", 4, attrs.wisdomMod);
        assertEquals("Score 3 should give -4", -4, attrs.charismaMod);
    }

    @Test
    public void toEntity_setsInitialValuesCorrectly() {
        CharacterCreationDTO dto = new CharacterCreationDTO();
        dto.name = "Grog";
        dto.gameSystem = "5e";
        dto.classAssignments = new ArrayList<>();
        
        CharacterCreationDTO.ClassAssignmentDTO ca = new CharacterCreationDTO.ClassAssignmentDTO();
        ca.level = 3;
        dto.classAssignments.add(ca);

        CharacterEntity entity = CharacterMapper.toEntity(dto);

        assertEquals("Grog", entity.name);
        assertEquals(3, entity.totalLevel);
        assertEquals(0, entity.experience);
        assertEquals(0, entity.currentHp);
    }
}
