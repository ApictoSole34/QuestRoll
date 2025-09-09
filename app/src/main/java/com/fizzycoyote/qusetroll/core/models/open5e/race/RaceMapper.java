package com.fizzycoyote.qusetroll.core.models.open5e.race;

import com.fizzycoyote.qusetroll.core.models.open5e.race.trait.TraitDto;
import com.fizzycoyote.qusetroll.core.models.open5e.race.trait.TraitEntity;

import java.util.ArrayList;
import java.util.List;

public class RaceMapper {
    public static RaceEntity dtoToEntity(RaceDto dto) {
        RaceEntity entity = new RaceEntity();
        entity.key = dto.key;
        entity.url = dto.url;
        entity.isSubrace = dto.isSubrace;
        entity.document = dto.document;
        entity.name = dto.name;
        entity.desc = dto.desc;
        entity.subraceOf = dto.subraceOf;
        return entity;
    }

    public static List<TraitEntity> dtoToEntity(String raceKey, List<TraitDto> dtos) {
        List<TraitEntity> entities = new ArrayList<>();
        for (TraitDto dto : dtos) {
            TraitEntity entity = new TraitEntity();
            entity.raceKey = raceKey;
            entity.name = dto.name;
            entity.desc = dto.desc;
            entities.add(entity);
        }
        return entities;
    }
}
