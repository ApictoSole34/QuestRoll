package com.fizzycoyote.qusetroll.core.models.open5e.spell;

public class SpellMapper {
    public static SpellEntity dtoToEntity(SpellDto dto) {
        SpellEntity e = new SpellEntity();
        e.key = dto.key;
        e.url = dto.url;
        e.document = dto.document;
        e.castingOptions = dto.castingOptions;
        e.school = dto.school;
        e.classes = dto.classes;
        e.rangeUnit = dto.rangeUnit;
        e.shapeSizeUnit = dto.shapeSizeUnit;
        e.name = dto.name;
        e.desc = dto.desc;
        e.level = dto.level;
        e.higherLevel = dto.higherLevel;
        e.targetType = dto.targetType;
        e.rangeText = dto.rangeText;
        e.range = dto.range;
        e.ritual = dto.ritual;
        e.castingTime = dto.castingTime;
        e.reactionCondition = dto.reactionCondition;
        e.verbal = dto.verbal;
        e.somatic = dto.somatic;
        e.material = dto.material;
        e.materialSpecified = dto.materialSpecified;
        e.materialCost = dto.materialCost;
        e.materialConsumed = dto.materialConsumed;
        e.targetCount = dto.targetCount;
        e.savingThrowAbility = dto.savingThrowAbility;
        e.attackRoll = dto.attackRoll;
        e.damageRoll = dto.damageRoll;
        e.damageTypes = dto.damageTypes;
        e.duration = dto.duration;
        e.shapeType = dto.shapeType;
        e.shapeSize = dto.shapeSize;
        e.concentration = dto.concentration;
        return e;
    }
}
