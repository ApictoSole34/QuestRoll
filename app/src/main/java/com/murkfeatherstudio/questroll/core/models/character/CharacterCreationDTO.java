package com.murkfeatherstudio.questroll.core.models.character;

import java.util.List;
import java.util.Map;

public class CharacterCreationDTO {
    public String name;
    public String gameSystem;          // "5e"
    public String alignmentKey;
    public String backgroundKey;
    public String speciesKey;

    public Map<String, Integer> attributes; // "STR", "DEX", "CON", "INT", "WIS", "CHA"

    public List<ClassAssignmentDTO> classAssignments;

    public List<InventoryItemDTO> startingItems;

    public List<TraitDTO> startingTraits;

    public List<String> startingSpellKeys;

    public static class ClassAssignmentDTO {
        public String classKey;
        public int level;
    }

    public static class InventoryItemDTO {
        public String itemKey;
        public String customName;
        public String customDescription;
        public float customWeight;
        public float customCost;
        public int quantity;
        public boolean equipped;
        public String slot;
    }

    public static class TraitDTO {
        public String sourceType;
        public String sourceKey;
        public String name;
        public String description;
        public int levelRequirement;
    }
}