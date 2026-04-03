package com.fizzycoyote.qusetroll.core.models.open5e.creature_type;

public class CreatureTypeMapper {
    public static CreatureTypeEntity dtoToEntity(CreatureTypeDto dto) {
        CreatureTypeEntity e = new CreatureTypeEntity();
        e.key = dto.key;
        e.name = dto.name;
        if (dto.descriptions != null && !dto.descriptions.isEmpty()) {
            e.description = dto.descriptions.get(0).desc;
        }
        e.documentName = dto.document;
        return e;
    }
}