package com.fizzycoyote.qusetroll.core.models.open5e.spell_school;

public class SpellSchoolMapper {
    public static SpellSchoolEntity dtoToEntity(SpellSchoolDto dto) {
        SpellSchoolEntity entity = new SpellSchoolEntity();
        String[] urlParts = dto.url.split("/");
        entity.slug = urlParts[urlParts.length - 2];
        entity.url = dto.url;
        entity.name = dto.name;
        entity.description = dto.description;
        entity.documentUrl = dto.documentUrl;

        return entity;
    }
}
