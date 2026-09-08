package com.murkfeatherstudio.questroll.core.models.open5e.rule_set;

import com.murkfeatherstudio.questroll.core.database.base.Mapper;

public class RulesetMapper implements Mapper<RulesetEntity, RulesetDto> {

    @Override
    public RulesetEntity toEntity(RulesetDto dto) {
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
