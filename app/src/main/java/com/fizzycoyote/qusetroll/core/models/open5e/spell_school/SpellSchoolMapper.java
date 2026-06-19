package com.fizzycoyote.qusetroll.core.models.open5e.spell_school;

public class SpellSchoolMapper {

    public static SpellSchoolEntity dtoToEntity(SpellSchoolDto dto) {
        SpellSchoolEntity entity = new SpellSchoolEntity();

        entity.key = dto.key;
        entity.name = dto.name;
        entity.desc = dto.desc;
        entity.document = dto.document;

        return entity;
    }
}