package com.fizzycoyote.qusetroll.feature_character.utils;

import android.util.Log;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BonusParser {

    public static int[] parseAbilityBonusesFromTraits(String traitsJson) {
        int[] bonuses = new int[6]; // STR,DEX,CON,INT,WIS,CHA
        if (traitsJson == null || traitsJson.isEmpty()) return bonuses;

        Gson gson = new Gson();
        Type listType = new TypeToken<List<Map<String, Object>>>() {}.getType();
        try {
            List<Map<String, Object>> traits = gson.fromJson(traitsJson, listType);
            for (Map<String, Object> trait : traits) {
                String name = (String) trait.get("name");
                String desc = (String) trait.get("desc");
                if (name == null) name = "";
                if (desc == null) desc = "";
                if (name.equals("Ability Score Increase") || desc.contains("Ability Score Increase")) {
                    extractBonusesFromText(desc, bonuses);
                }
            }
        } catch (Exception e) {
            Log.e("BonusParser", "Error parsing traits JSON", e);
        }
        return bonuses;
    }

    public static int[] parseAbilityBonusesFromBenefits(String benefitsJson) {
        int[] bonuses = new int[6];
        if (benefitsJson == null || benefitsJson.isEmpty()) return bonuses;

        Gson gson = new Gson();
        Type listType = new TypeToken<List<Map<String, Object>>>() {}.getType();
        try {
            List<Map<String, Object>> benefits = gson.fromJson(benefitsJson, listType);
            for (Map<String, Object> benefit : benefits) {
                String name = (String) benefit.get("name");
                String desc = (String) benefit.get("desc");
                if (name == null) name = "";
                if (desc == null) desc = "";
                if (name.equals("Ability Score Increases") || desc.contains("Ability Score Increases")) {
                    extractBonusesFromText(desc, bonuses);
                }
            }
        } catch (Exception e) {
            Log.e("BonusParser", "Error parsing benefits JSON", e);
        }
        return bonuses;
    }

    private static void extractBonusesFromText(String text, int[] bonuses) {
        String[] abilities = {"Strength", "Dexterity", "Constitution", "Intelligence", "Wisdom", "Charisma"};
        String[] keys = {"STR", "DEX", "CON", "INT", "WIS", "CHA"};
        // Pattern: "Constitution score increases by 1"
        for (int i = 0; i < abilities.length; i++) {
            Pattern p = Pattern.compile(abilities[i] + " score increases by (\\d+)", Pattern.CASE_INSENSITIVE);
            Matcher m = p.matcher(text);
            if (m.find()) {
                bonuses[i] = Integer.parseInt(m.group(1));
            }
        }
        // Pattern: "+1 to Wisdom and one other ability score." – add only the first found
        Pattern general = Pattern.compile("\\+(\\d+) to (\\w+)", Pattern.CASE_INSENSITIVE);
        Matcher gm = general.matcher(text);
        while (gm.find()) {
            int bonus = Integer.parseInt(gm.group(1));
            String ability = gm.group(2).toLowerCase();
            for (int i = 0; i < abilities.length; i++) {
                if (abilities[i].toLowerCase().startsWith(ability) || ability.startsWith(abilities[i].toLowerCase().substring(0, 3))) {
                    bonuses[i] = bonus;
                    break;
                }
            }
        }
    }
}