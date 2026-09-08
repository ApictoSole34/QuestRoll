package com.murkfeatherstudio.questroll.core.models.open5e.spell_school;

import com.murkfeatherstudio.questroll.core.database.base.Mapper;

public class SpellSchoolMapper implements Mapper<SpellSchoolEntity, SpellSchoolDto> {

    @Override
    public SpellSchoolEntity toEntity(SpellSchoolDto dto) {
        SpellSchoolEntity entity = new SpellSchoolEntity();

        entity.key = dto.key;
        entity.name = dto.name;
        entity.desc = dto.desc;
        entity.document = dto.document;

        return entity;
    }
}
