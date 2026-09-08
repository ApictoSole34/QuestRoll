package com.murkfeatherstudio.questroll.core.models.open5e.feat;

import com.murkfeatherstudio.questroll.core.database.base.Mapper;

public class FeatMapper implements Mapper<FeatEntity, FeatDto> {
    @Override
    public FeatEntity toEntity(FeatDto dto) {
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
