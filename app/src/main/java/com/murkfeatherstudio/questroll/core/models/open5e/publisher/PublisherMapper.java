package com.murkfeatherstudio.questroll.core.models.open5e.publisher;

import com.murkfeatherstudio.questroll.core.database.base.Mapper;

public class PublisherMapper implements Mapper<PublisherEntity, PublisherDto> {
    @Override
    public PublisherEntity toEntity(PublisherDto dto) {
        PublisherEntity entity = new PublisherEntity();
        entity.key = dto.key;
        entity.url = dto.url;
        entity.name = dto.name;
        return entity;
    }
}
