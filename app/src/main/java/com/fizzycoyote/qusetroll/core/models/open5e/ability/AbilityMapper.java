package com.fizzycoyote.qusetroll.core.models.open5e.ability;

import com.fizzycoyote.qusetroll.core.models.open5e.ability.skill.SkillEntity;
import com.google.gson.Gson;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class AbilityMapper {
    private static final Gson gson = new Gson();

    public static AbilityEntity dtoToEntity(AbilityDto dto) {
        AbilityEntity e = new AbilityEntity();
        e.key = dto.key;
        e.name = dto.name;
        e.shortDesc = dto.shortDesc;
        e.descriptionsJson = gson.toJson(dto.descriptions);
        return e;
    }

    public static List<SkillEntity> dtosToSkillEntities(AbilityDto dto) {
        if (dto.skills == null) return Collections.emptyList();
        return dto.skills.stream()
                .map(s -> {
                    SkillEntity se = new SkillEntity();
                    se.key = s.key;
                    se.name = s.name;
                    se.abilityKey = dto.key;
                    se.documentKey = s.document;
                    se.descriptionsJson = gson.toJson(s.descriptions);
                    return se;
                })
                .collect(Collectors.toList());
    }
}