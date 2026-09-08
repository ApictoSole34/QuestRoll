package com.murkfeatherstudio.questroll.feature_ability.model;

import static org.junit.Assert.*;
import org.junit.Test;
import com.murkfeatherstudio.questroll.core.models.open5e.ability.AbilityEntity;
import com.murkfeatherstudio.questroll.core.models.custom.custom_ability.CustomAbilityEntity;
import com.murkfeatherstudio.questroll.core.models.open5e.ability.skill.SkillEntity;
import com.murkfeatherstudio.questroll.core.models.custom.custom_ability.CustomSkillEntity;

public class CombinedModelsTest {

    @Test
    public void combinedAbility_mapsFromSrdCorrectly() {
        AbilityEntity srd = new AbilityEntity();
        srd.key = "STR";
        srd.name = "Strength";
        srd.shortDesc = "Muscle power";
        
        CombinedAbility combined = new CombinedAbility(srd);
        
        assertEquals("STR", combined.id);
        assertFalse(combined.isCustom);
        assertEquals("Strength", combined.name);
    }

    @Test
    public void combinedAbility_mapsFromCustomCorrectly() {
        CustomAbilityEntity custom = new CustomAbilityEntity();
        custom.id = 101L;
        custom.name = "Honor";
        custom.shortDesc = "Social standing";
        
        CombinedAbility combined = new CombinedAbility(custom);
        
        assertEquals("custom_101", combined.id);
        assertTrue(combined.isCustom);
        assertEquals(101L, combined.customId);
    }

    @Test
    public void combinedSkill_mapsFromSrdCorrectly() {
        SkillEntity srd = new SkillEntity();
        srd.key = "ath";
        srd.name = "Athletics";
        
        CombinedSkill combined = new CombinedSkill(srd, "Strength");
        
        assertEquals("ath", combined.key);
        assertEquals("Strength", combined.abilityName);
        assertFalse(combined.isCustom);
    }

    @Test
    public void combinedSkill_mapsFromCustomCorrectly() {
        CustomSkillEntity custom = new CustomSkillEntity();
        custom.id = 50L;
        custom.name = "Engineering";
        custom.abilityName = "Intelligence";
        
        CombinedSkill combined = new CombinedSkill(custom);
        
        assertEquals("custom_50", combined.id);
        assertTrue(combined.isCustom);
        assertEquals("Intelligence", combined.abilityName);
    }
}
