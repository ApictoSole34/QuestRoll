package com.murkfeatherstudio.questroll.feature_character.utils;

import com.murkfeatherstudio.questroll.core.models.character.CharacterCreationDTO;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for parsing character benefits from JSON data provided by the Open5e API.
 * <p>
 * This class extracts structured information such as skill proficiencies, languages,
 * tool proficiencies, and starting equipment from descriptive text and JSON structures
 * associated with backgrounds and classes.
 * </p>
 */
public class BenefitParser {

    private static final Gson gson = new Gson();

    /**
     * Parses a JSON string of benefits and categorizes them into a {@link ParsedBenefits} object.
     *
     * @param benefitsJson The raw JSON string containing benefit definitions.
     * @return A {@link ParsedBenefits} object containing the extracted data.
     */
    public static ParsedBenefits parseBenefits(String benefitsJson) {
        ParsedBenefits result = new ParsedBenefits();
        if (benefitsJson == null || benefitsJson.isEmpty()) return result;

        Type listType = new TypeToken<List<Map<String, Object>>>() {}.getType();
        try {
            List<Map<String, Object>> benefits = gson.fromJson(benefitsJson, listType);
            for (Map<String, Object> benefit : benefits) {
                String type = (String) benefit.get("type");
                String desc = (String) benefit.get("desc");
                if (desc == null) continue;

                if ("equipment".equals(type)) {
                    result.equipmentDescription = desc;
                    Pattern goldPattern = Pattern.compile("(\\d+)\\s*gp");
                    Matcher goldMatcher = goldPattern.matcher(desc);
                    if (goldMatcher.find()) {
                        result.gold = Integer.parseInt(goldMatcher.group(1));
                    }
                } else if ("skill_proficiency".equals(type)) {
                    parseSkillProficiencies(desc, result);
                } else if ("language".equals(type)) {
                    parseLanguages(desc, result);
                } else if ("tool_proficiency".equals(type)) {
                    parseToolProficiencies(desc, result);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    private static void parseSkillProficiencies(String desc, ParsedBenefits result) {
        String[] parts = desc.split(",");
        for (String part : parts) {
            String trimmed = part.trim();
            if (trimmed.startsWith("and ")) trimmed = trimmed.substring(4);
            if (!trimmed.isEmpty()) result.skillProficiencies.add(trimmed);
        }
    }

    private static void parseLanguages(String desc, ParsedBenefits result) {
        if (desc == null) return;
        String lower = desc.toLowerCase();
        if (lower.contains("one of your choice")) {
            result.languageChoices = 1;
        } else if (lower.contains("two of your choice")) {
            result.languageChoices = 2;
        } else if (lower.contains("no additional languages")) {
            // nothing
        } else {
            // Assume explicit language names, e.g. "Dwarvish, Elvish"
            String[] parts = desc.split(",");
            for (String part : parts) {
                String lang = part.trim();
                if (!lang.isEmpty()) result.fixedLanguages.add(lang);
            }
        }
    }

    private static void parseToolProficiencies(String desc, ParsedBenefits result) {
        String[] parts = desc.split(",");
        for (String part : parts) {
            String trimmed = part.trim();
            if (trimmed.startsWith("and ")) trimmed = trimmed.substring(4);
            if (!trimmed.isEmpty()) result.toolProficiencies.add(trimmed);
        }
    }

    /**
     * Parses a class's starting equipment description into a list of DTOs.
     * <p>
     * Cleans up the text by removing introductory boilerplate and parsing comma-separated items.
     * </p>
     *
     * @param equipmentDesc The raw description of starting equipment.
     * @return A list of {@link com.murkfeatherstudio.questroll.core.models.character.CharacterCreationDTO.InventoryItemDTO}.
     */
    public static List<CharacterCreationDTO.InventoryItemDTO> parseClassEquipment(String equipmentDesc) {
        List<CharacterCreationDTO.InventoryItemDTO> items = new ArrayList<>();
        if (equipmentDesc == null || equipmentDesc.isEmpty()) return items;

        // Remove headers and markers
        equipmentDesc = equipmentDesc.replaceAll("(?i)You start with the following equipment,?\\s*", "");
        equipmentDesc = equipmentDesc.replaceAll("(?i)in addition to the equipment granted by your background:\\s*", "");
        equipmentDesc = equipmentDesc.replaceAll("(?i)You start with the following equipment:\\s*", "");
        equipmentDesc = equipmentDesc.replaceAll("\\(\\*?[a-z]\\*?\\)", "");
        equipmentDesc = equipmentDesc.replaceAll("\\*", "");
        equipmentDesc = equipmentDesc.replaceAll("\\r?\\n", " ");
        equipmentDesc = equipmentDesc.replaceAll("\\s+", " ").trim();

        String[] parts = equipmentDesc.split(",");
        for (String part : parts) {
            part = part.trim();
            if (part.isEmpty()) continue;
            if (part.startsWith("and ")) part = part.substring(4);
            CharacterCreationDTO.InventoryItemDTO item = new CharacterCreationDTO.InventoryItemDTO();
            item.customName = part;
            item.quantity = 1;
            items.add(item);
        }
        return items;
    }

    /**
     * Data class holding structured benefit data extracted from background or class descriptions.
     */
    public static class ParsedBenefits {
        public String equipmentDescription = "";
        public int gold = 0;
        public List<String> skillProficiencies = new ArrayList<>();
        public List<String> fixedLanguages = new ArrayList<>();
        public int languageChoices = 0;
        public List<String> toolProficiencies = new ArrayList<>();
    }
}
