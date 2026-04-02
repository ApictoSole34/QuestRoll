package com.fizzycoyote.qusetroll.core.models.open5e.environment;

public class EnvironmentMapper {
    public static EnvironmentEntity dtoToEntity(EnvironmentDto dto) {
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