package com.murkfeatherstudio.questroll.core.models.open5e.spell_list;

import com.murkfeatherstudio.questroll.core.database.base.Mapper;

public class SpellListMapper implements Mapper<SpellListEntity, SpellListDto> {
    @Override
    public SpellListEntity toEntity(SpellListDto dto) {
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
