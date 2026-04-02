package com.fizzycoyote.qusetroll.core.models.open5e.weapon_property;

public class WeaponPropertyMapper {
    public static WeaponPropertyEntity dtoToEntity(WeaponPropertyDto dto) {
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