package com.fizzycoyote.qusetroll.core.models.open5e.feat;

public class FeatMapper {
    public static FeatEntity dtoToEntity(FeatDto dto) {
        FeatEntity entity = new FeatEntity();
        entity.key = dto.key;
        entity.url = dto.url;
        entity.hasPrerequisite = dto.hasPrerequisite;
        entity.document = dto.document;
        entity.name = dto.name;
        entity.desc = dto.desc;
        entity.prerequisites = dto.prerequisites;
        return entity;
    }
}
