package com.fizzycoyote.qusetroll.core.models.open5e.weapon;

import com.google.gson.Gson;

public class WeaponMapper {
    public static WeaponEntity dtoToEntity(WeaponDto dto) {
        WeaponEntity e = new WeaponEntity();
        e.key = dto.key;
        e.name = dto.name;
        e.damageDice = dto.damageDice;
        e.range = dto.range;
        e.longRange = dto.longRange;
        e.isSimple = dto.isSimple;
        e.isImprovised = dto.isImprovised;
        if (dto.damageType != null) {
            e.damageTypeName = dto.damageType.name;
            e.damageTypeKey = dto.damageType.key;
        }
        if (dto.properties != null) {
            e.propertiesJson = new Gson().toJson(dto.properties);
        }
        if (dto.document != null) {
            e.documentName = dto.document.name;
            e.documentKey = dto.document.key;
        }
        return e;
    }
}