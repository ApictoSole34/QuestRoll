package com.fizzycoyote.qusetroll.core.models.open5e.creature;

import com.google.gson.Gson;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class CreatureMapper {

    public static CreatureEntity dtoToEntity(CreatureDto dto) {
        CreatureEntity e = new CreatureEntity();
        Gson gson = new Gson();

        e.key = dto.key;
        e.name = dto.name;
        e.alignment = dto.alignment;
        e.category = dto.category;
        e.armorClass = dto.armorClass;
        e.armorDetail = dto.armorDetail;
        e.hitPoints = dto.hitPoints;
        e.hitDice = dto.hitDice;
        e.experiencePoints = dto.experiencePoints;
        e.initiativeBonus = dto.initiativeBonus;
        e.passivePerception = dto.passivePerception;
        e.darkvisionRange = dto.darkvisionRange;
        e.blindsightRange = dto.blindsightRange;
        e.tremorsenseRange = dto.tremorsenseRange;
        e.truesightRange = dto.truesightRange;

        if (dto.challengeRating != null) {
            e.challengeRatingDecimal = dto.challengeRating;
        } else {
            e.challengeRatingDecimal = 0f;
        }
        e.challengeRatingText = formatChallengeRatingText(e.challengeRatingDecimal);

        if (dto.type != null) {
            e.typeName = dto.type.name;
            e.typeKey = dto.type.key;
        }
        if (dto.size != null) {
            e.sizeName = dto.size.name;
            e.sizeKey = dto.size.key;
        }

        if (dto.abilityScores != null) {
            e.str = dto.abilityScores.strength;
            e.dex = dto.abilityScores.dexterity;
            e.con = dto.abilityScores.constitution;
            e.intScore = dto.abilityScores.intelligence;
            e.wis = dto.abilityScores.wisdom;
            e.cha = dto.abilityScores.charisma;
        }

        if (dto.modifiers != null) {
            e.strMod = dto.modifiers.strength;
            e.dexMod = dto.modifiers.dexterity;
            e.conMod = dto.modifiers.constitution;
            e.intMod = dto.modifiers.intelligence;
            e.wisMod = dto.modifiers.wisdom;
            e.chaMod = dto.modifiers.charisma;
        }

        if (dto.speed != null) {
            e.speedJson = gson.toJson(dto.speed);
        }

        if (dto.savingThrows != null) {
            e.savingThrowsJson = gson.toJson(dto.savingThrows);
        }

        if (dto.skillBonuses != null) {
            e.skillBonusesJson = gson.toJson(dto.skillBonuses);
        }

        if (dto.languages != null) {
            e.languages = dto.languages.asString;
        }

        if (dto.resistancesAndImmunities != null) {
            e.damageImmunities = dto.resistancesAndImmunities.damageImmunitiesDisplay;
            e.damageResistances = dto.resistancesAndImmunities.damageResistancesDisplay;
            e.damageVulnerabilities = dto.resistancesAndImmunities.damageVulnerabilitiesDisplay;
            e.conditionImmunities = dto.resistancesAndImmunities.conditionImmunitiesDisplay;
        }

        if (dto.actions != null) {
            List<CreatureDto.CreatureActionDto> sorted = dto.actions.stream()
                    .sorted(Comparator.comparing(a -> a.orderInStatblock))
                    .collect(Collectors.toList());
            e.actionsJson = gson.toJson(sorted);
        }

        if (dto.traits != null) {
            e.traitsJson = gson.toJson(dto.traits);
        }

        if (dto.document != null) {
            e.documentName = dto.document.name;
            e.documentKey = dto.document.key;
        }

        return e;
    }

    private static String formatChallengeRatingText(float cr) {
        if (Math.abs(cr - 0.125f) < 0.001f) return "1/8";
        if (Math.abs(cr - 0.25f) < 0.001f) return "1/4";
        if (Math.abs(cr - 0.5f) < 0.001f) return "1/2";
        if (cr == Math.floor(cr)) return String.valueOf((int) cr);
        return String.valueOf(cr);
    }
}