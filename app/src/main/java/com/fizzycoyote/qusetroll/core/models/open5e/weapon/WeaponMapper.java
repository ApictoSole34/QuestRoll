package com.fizzycoyote.qusetroll.core.models.open5e.weapon;

public class WeaponMapper {
    public static WeaponEntity dtoToEntity(WeaponDto dto) {
        WeaponEntity entity = new WeaponEntity();
        entity.key = dto.key;
        entity.url = dto.url;
        entity.name = dto.name;
        entity.damageDice = dto.damageDice;
        entity.versatileDice = dto.versatileDice;
        entity.isVersatile = dto.isVersatile;
        entity.isMartial = dto.isMartial;
        entity.isMelee = dto.isMelee;
        entity.rangedAttackPossible = dto.rangedAttackPossible;
        entity.reach = dto.reach;
        entity.range = dto.range;
        entity.longRange = dto.longRange;
        entity.isFinesse = dto.isFinesse;
        entity.isThrown = dto.isThrown;
        entity.isTwoHanded = dto.isTwoHanded;
        entity.requiresAmmunition = dto.requiresAmmunition;
        entity.requiresLoading = dto.requiresLoading;
        entity.isHeavy = dto.isHeavy;
        entity.isLight = dto.isLight;
        entity.isSimple = dto.isSimple;
        entity.isImprovised = dto.isImprovised;
        entity.properties = dto.properties;
        entity.damageTypeUrl = dto.damageTypeUrl;
        entity.documentUrl = dto.documentUrl;
        return entity;
    }

}
