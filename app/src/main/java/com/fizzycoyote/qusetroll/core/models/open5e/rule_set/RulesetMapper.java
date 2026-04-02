package com.fizzycoyote.qusetroll.core.models.open5e.rule_set;

public class RulesetMapper {
    public static RulesetEntity dtoToEntity(RulesetDto dto) {
        RulesetEntity e = new RulesetEntity();
        e.key = dto.key;
        e.name = dto.name;
        e.desc = dto.desc;
        if (dto.document != null) {
            e.documentName = dto.document.name;
            e.documentKey = dto.document.key;
        }
        return e;
    }
}