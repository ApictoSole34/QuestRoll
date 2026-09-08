package com.murkfeatherstudio.questroll.core.models.open5e.weapon_property;

import com.murkfeatherstudio.questroll.core.database.base.Mapper;

public class WeaponPropertyMapper implements Mapper<WeaponPropertyEntity, WeaponPropertyDto> {
    @Override
    public WeaponPropertyEntity toEntity(WeaponPropertyDto dto) {
        WeaponPropertyEntity e = new WeaponPropertyEntity();
        e.key = dto.key;
        e.name = dto.name;
        e.desc = dto.desc;
        e.document = dto.document;
        e.url = dto.url;
        e.type = dto.type;
        return e;
    }
}
