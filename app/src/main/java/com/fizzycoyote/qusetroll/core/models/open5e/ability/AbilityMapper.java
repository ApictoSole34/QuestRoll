package com.fizzycoyote.qusetroll.core.models.open5e.ability;

import com.fizzycoyote.qusetroll.core.models.open5e.ability.skill.SkillEntity;

import java.util.List;
import java.util.stream.Collectors;

public class AbilityMapper {
    public static AbilityEntity toEntity(AbilityDto dto) {
        AbilityEntity entity = new AbilityEntity();
        entity.key = dto.key;
        entity.name = dto.name;
        entity.description = dto.description;
        entity.shortDesc = dto.shortDesc;
        entity.documentUrl = dto.documentUrl;
        return entity;
    }

    public static List<SkillEntity> toSkillEntities(AbilityDto abilityDto) {
        return abilityDto.skills.stream()
                .map(skillDto -> {
                    SkillEntity entity = new SkillEntity();
                    entity.key = skillDto.key;
                    entity.abilityKey = abilityDto.key;
                    entity.name = skillDto.name;
                    entity.description = skillDto.description;
                    return entity;
                })
                .collect(Collectors.toList());
    }
}