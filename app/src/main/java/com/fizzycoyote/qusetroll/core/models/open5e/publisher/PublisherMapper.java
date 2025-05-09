package com.fizzycoyote.qusetroll.core.models.open5e.publisher;

public class PublisherMapper {
    public static PublisherEntity dtoToEntity(PublisherDto dto) {
        PublisherEntity entity = new PublisherEntity();
        entity.key = dto.key;
        entity.url = dto.url;
        entity.name = dto.name;
        return entity;
    }
}
