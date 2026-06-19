package com.fizzycoyote.qusetroll.core.models.open5e.rule;

public class RuleMapper {

    public static RuleEntity dtoToEntity(RuleDto dto) {

        RuleEntity e = new RuleEntity();

        e.key = dto.key;
        e.name = dto.name;
        e.desc = dto.desc;
        e.index = dto.index;
        e.initialHeaderLevel = dto.initialHeaderLevel;
        e.documentUrl = dto.document;
        e.rulesetKey = dto.ruleset;

        return e;
    }
}