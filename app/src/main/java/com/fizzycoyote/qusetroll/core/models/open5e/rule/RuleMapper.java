package com.fizzycoyote.qusetroll.core.models.open5e.rule;

public class RuleMapper {
    public static RuleEntity dtoToEntity(RuleDto dto) {
        RuleEntity e = new RuleEntity();
        e.url = dto.url;
        e.name = dto.name;
        e.desc = dto.desc;
        e.index = dto.index;
        e.initialHeaderLevel = dto.initialHeaderLevel;
        e.documentUrl = dto.document;
        if (dto.ruleset != null) {
            String[] parts = dto.ruleset.split("/");
            if (parts.length > 0) e.rulesetKey = parts[parts.length - 1];
        }
        return e;
    }
}