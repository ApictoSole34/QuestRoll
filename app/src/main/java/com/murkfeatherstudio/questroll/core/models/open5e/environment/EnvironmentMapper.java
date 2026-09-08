package com.murkfeatherstudio.questroll.core.models.open5e.environment;

import com.murkfeatherstudio.questroll.core.database.base.Mapper;

public class EnvironmentMapper implements Mapper<EnvironmentEntity, EnvironmentDto> {

    @Override
    public EnvironmentEntity toEntity(EnvironmentDto dto) {
        EnvironmentEntity e = new EnvironmentEntity();
        e.key = dto.key;
        e.name = dto.name;
        e.desc = dto.desc;
        e.aquatic = dto.aquatic;
        e.planar = dto.planar;
        e.interior = dto.interior;
        e.url = dto.url;
        e.document = dto.document;
        return e;
    }
}
