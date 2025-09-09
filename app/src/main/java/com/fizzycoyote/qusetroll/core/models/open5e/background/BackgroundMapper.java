package com.fizzycoyote.qusetroll.core.models.open5e.background;

public class BackgroundMapper {
    public static BackgroundEntity dtoToEntity(BackgroundDto dto) {
        BackgroundEntity entity = new BackgroundEntity();
        entity.key = dto.key;
        entity.url = dto.url;
        entity.document = dto.document;
        entity.name = dto.name;
        entity.desc = dto.desc;
        return entity;
    }
}
