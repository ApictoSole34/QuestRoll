package com.murkfeatherstudio.questroll.core.models.open5e.creature_type;

import com.murkfeatherstudio.questroll.core.database.base.Mapper;

public class CreatureTypeMapper implements Mapper<CreatureTypeEntity, CreatureTypeDto> {

    @Override
    public CreatureTypeEntity toEntity(CreatureTypeDto dto) {
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
