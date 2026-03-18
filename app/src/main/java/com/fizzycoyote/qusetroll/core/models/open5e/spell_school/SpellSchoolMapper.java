package com.fizzycoyote.qusetroll.core.models.open5e.spell_school;

public class SpellSchoolMapper {
    public static SpellSchoolEntity dtoToEntity(SpellSchoolDto dto) {
        SpellSchoolEntity entity = new SpellSchoolEntity();

        String url = dto.url.endsWith("/")
                ? dto.url.substring(0, dto.url.length() - 1)
                : dto.url;
        String[] parts = url.split("/");
        entity.slug = parts[parts.length - 1];

        entity.url = dto.url;
        entity.name = dto.name;
        entity.description = dto.description;
        entity.documentUrl = dto.documentUrl;

        return entity;
    }
}
