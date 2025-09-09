package com.fizzycoyote.qusetroll.core.models.open5e.damage_type;

public class DamageTypeMapper {
    public static DamageTypeEntity dtoToEntity(DamageTypeDto dto) {
        DamageTypeEntity e = new DamageTypeEntity();
        e.key = dto.key;
        e.url = dto.url;
        e.name = dto.name;
        e.desc = dto.desc;
        e.documentUrl = dto.documentUrl;
        return e;
    }
}