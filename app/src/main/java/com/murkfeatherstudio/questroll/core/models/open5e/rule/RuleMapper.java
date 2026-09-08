package com.murkfeatherstudio.questroll.core.models.open5e.rule;

import com.murkfeatherstudio.questroll.core.database.base.Mapper;

public class RuleMapper implements Mapper<RuleEntity, RuleDto> {

    @Override
    public RuleEntity toEntity(RuleDto dto) {
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
