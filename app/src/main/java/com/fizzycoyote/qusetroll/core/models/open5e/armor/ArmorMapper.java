package com.fizzycoyote.qusetroll.core.models.open5e.armor;

public class ArmorMapper {
    public static ArmorEntity dtoToEntity(ArmorDto dto) {
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
