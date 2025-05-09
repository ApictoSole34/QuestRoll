package com.fizzycoyote.qusetroll.core.models.open5e.license;

public class LicenseMapper {
    public static LicenseEntity dtoToEntity(LicenseDto dto) {
        LicenseEntity entity = new LicenseEntity();
        entity.key = dto.key;
        entity.url = dto.url;
        entity.name = dto.name;
        entity.desc = dto.desc;
        return entity;
    }
}
