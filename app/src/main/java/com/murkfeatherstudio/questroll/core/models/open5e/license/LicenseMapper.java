package com.murkfeatherstudio.questroll.core.models.open5e.license;

import com.murkfeatherstudio.questroll.core.database.base.Mapper;

public class LicenseMapper implements Mapper<LicenseEntity, LicenseDto> {
    @Override
    public LicenseEntity toEntity(LicenseDto dto) {
        LicenseEntity entity = new LicenseEntity();
        entity.key = dto.key;
        entity.url = dto.url;
        entity.name = dto.name;
        entity.desc = dto.desc;
        return entity;
    }
}
