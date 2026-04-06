package com.fizzycoyote.qusetroll.core.models.open5e.spell;

import com.google.gson.Gson;

import java.util.stream.Collectors;

public class SpellMapper {

    public static SpellEntity dtoToEntity(SpellDto dto) {
        SpellEntity e = new SpellEntity();

        e.key = dto.key;
        e.name = dto.name;
        e.desc = dto.desc;
        e.level = dto.level;
        e.higherLevel = dto.higherLevel;
        e.castingTime = dto.castingTime;
        e.duration = dto.duration;
        e.rangeText = dto.rangeText;
        e.range = dto.range;
        e.ritual = dto.ritual;
        e.concentration = dto.concentration;
        e.verbal = dto.verbal;
        e.somatic = dto.somatic;
        e.material = dto.material;
        e.materialSpecified = dto.materialSpecified;
        e.reactionCondition = dto.reactionCondition;
        e.targetType = dto.targetType;
        e.targetCount = dto.targetCount;
        e.savingThrowAbility = dto.savingThrowAbility;
        e.attackRoll = dto.attackRoll;
        e.damageRoll = dto.damageRoll;
        e.damageTypes = dto.damageTypes;

        if (dto.school != null) {
            e.schoolName = dto.school.name;
            e.schoolKey = dto.school.key;
        }

        if (dto.classes != null) {
            e.classes = dto.classes.stream()
                    .map(c -> c.name)
                    .collect(Collectors.toList());
        }

        if (dto.document != null) {
            e.documentName = dto.document.name;
            if (dto.document.key != null && !dto.document.key.isEmpty()) {
                e.documentKey = dto.document.key;
            } else if (dto.document.permalink != null && !dto.document.permalink.isEmpty()) {
                String permalink = dto.document.permalink;
                if (permalink.endsWith("/")) {
                    permalink = permalink.substring(0, permalink.length() - 1);
                }
                String[] parts = permalink.split("/");
                if (parts.length > 0) {
                    e.documentKey = parts[parts.length - 1];
                }
            }
        }

        if (dto.castingOptions != null) {
            e.castingOptionsJson = new Gson().toJson(dto.castingOptions);
        }

        return e;
    }
}