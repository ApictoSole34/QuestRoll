package com.murkfeatherstudio.questroll.core.models.open5e.damage_type;

import com.murkfeatherstudio.questroll.core.database.base.Mapper;

public class DamageTypeMapper implements Mapper<DamageTypeEntity, DamageTypeDto> {

    @Override
    public DamageTypeEntity toEntity(DamageTypeDto dto) {
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
