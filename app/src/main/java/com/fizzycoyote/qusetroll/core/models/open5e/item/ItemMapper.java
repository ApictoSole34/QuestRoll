package com.fizzycoyote.qusetroll.core.models.open5e.item;

public class ItemMapper {
    public static ItemEntity dtoToEntity(ItemDto dto) {
        ItemEntity entity = new ItemEntity();
        entity.key = dto.key;
        entity.url = dto.url;
        entity.isMagicItem = dto.isMagicItem;
        entity.weaponUrl = dto.weaponUrl;
        entity.armorUrl = dto.armorUrl;
        entity.document = dto.documentUrl;
        entity.category = dto.category;
        entity.rarity = dto.rarity;
        entity.name = dto.name;
        entity.desc = dto.desc;
        entity.weight = dto.weight;
        entity.armorClass = dto.armorClass;
        entity.hitPoints = dto.hitPoints;
        entity.hitDice = dto.hitDice;
        entity.nonmagicalAttackResistance = dto.nonmagicalAttackResistance;
        entity.nonmagicalAttackImmunity = dto.nonmagicalAttackImmunity;
        entity.cost = dto.cost;
        entity.requiresAttunement = dto.requaiersAttunement;
        entity.size = dto.size;
        entity.damageVulnerabilities = dto.damageVulnerabilites;
        entity.damageImmunities = dto.damageImmunities;
        entity.damageResistances = dto.damageResistances;
        return entity;
    }
}
