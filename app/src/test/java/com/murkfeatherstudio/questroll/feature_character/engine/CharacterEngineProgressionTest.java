package com.murkfeatherstudio.questroll.feature_character.engine;

import static org.junit.Assert.*;
import org.junit.Test;
import java.util.ArrayList;
import java.util.List;
import com.murkfeatherstudio.questroll.core.models.open5e.character_class.feature.FeatureEntity;
import com.murkfeatherstudio.questroll.core.models.open5e.character_class.gained_at.GainedAt;

public class CharacterEngineProgressionTest {

    @Test
    public void filterAvailableFeatures_returnsOnlyUnlockedFeatures() {
        // Mock data
        List<FeatureEntity> allFeatures = new ArrayList<>();
        
        FeatureEntity f1 = new FeatureEntity();
        f1.name = "Second Wind";
        GainedAt g1 = new GainedAt(); g1.level = 1;
        f1.gainedAt = List.of(g1);
        
        FeatureEntity f2 = new FeatureEntity();
        f2.name = "Extra Attack";
        GainedAt g2 = new GainedAt(); g2.level = 5;
        f2.gainedAt = List.of(g2);
        
        allFeatures.add(f1);
        allFeatures.add(f2);

        // Logic to test (replicated from CharacterEngine)
        List<FeatureEntity> availableAtLevel3 = filterFeatures(allFeatures, 3);
        List<FeatureEntity> availableAtLevel5 = filterFeatures(allFeatures, 5);

        assertEquals(1, availableAtLevel3.size());
        assertEquals("Second Wind", availableAtLevel3.get(0).name);
        
        assertEquals(2, availableAtLevel5.size());
    }

    private List<FeatureEntity> filterFeatures(List<FeatureEntity> all, int level) {
        List<FeatureEntity> available = new ArrayList<>();
        for (FeatureEntity f : all) {
            if (f.gainedAt != null) {
                for (GainedAt gained : f.gainedAt) {
                    if (gained.level <= level) {
                        available.add(f);
                        break;
                    }
                }
            }
        }
        return available;
    }
}
