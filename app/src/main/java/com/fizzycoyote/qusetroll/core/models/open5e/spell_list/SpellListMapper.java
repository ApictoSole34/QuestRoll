package com.fizzycoyote.qusetroll.core.models.open5e.spell_list;

public class SpellListMapper {
    public static SpellListEntity dtoToEntity(SpellListDto dto) {
        SpellListEntity entity = new SpellListEntity();
        entity.slug = dto.slug;
        entity.name = dto.name;
        entity.desc = dto.desc;
        entity.spells = dto.spells;
        entity.documentSlug = dto.documentSlug;
        entity.documentTitle = dto.documentTitle;
        entity.documentLicenseUrl = dto.documentLicenseUrl;
        entity.documentUrl = dto.documentUrl;
        return entity;
    }
}
