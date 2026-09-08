package com.murkfeatherstudio.questroll.core.models.open5e.armor;

import com.murkfeatherstudio.questroll.core.database.base.Mapper;

public class ArmorMapper implements Mapper<ArmorEntity, ArmorDto> {
    @Override
    public ArmorEntity toEntity(ArmorDto dto) {
        ArmorEntity entity = new ArmorEntity();
        entity.key = dto.key;
        entity.url = dto.url;
        entity.name = dto.name;
        entity.category = dto.category;
        entity.acDisplay = dto.acDisplay;
        entity.grantsStealthDisadvantage = dto.grantsStealthDisadvantage;
        entity.strengthRequirement = dto.strengthRequirement;
        entity.acBase = dto.acBase;
        entity.acAddDexxmod = dto.acAddDexxmod;
        entity.acCapDexmod = dto.acCapDexmod;
        entity.document = dto.document;
        return entity;
    }
}
