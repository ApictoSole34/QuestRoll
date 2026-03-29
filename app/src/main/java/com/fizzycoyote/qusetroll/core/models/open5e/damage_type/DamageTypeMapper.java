package com.fizzycoyote.qusetroll.core.models.open5e.damage_type;

public class DamageTypeMapper {
    public static DamageTypeEntity dtoToEntity(DamageTypeDto dto) {
        DamageTypeEntity e = new DamageTypeEntity();
        e.key = dto.key;
        e.name = dto.name;
        e.url = dto.url;
        if (dto.descriptions != null && !dto.descriptions.isEmpty()) {
            e.description = dto.descriptions.get(0).desc;
        }
        e.document = dto.document;
        return e;
    }
}