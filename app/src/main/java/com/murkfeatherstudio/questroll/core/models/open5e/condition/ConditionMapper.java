package com.murkfeatherstudio.questroll.core.models.open5e.condition;

import com.murkfeatherstudio.questroll.core.database.base.Mapper;

public class ConditionMapper implements Mapper<ConditionEntity, ConditionDto> {

    @Override
    public ConditionEntity toEntity(ConditionDto dto) {
        ConditionEntity e = new ConditionEntity();
        e.key = dto.key;
        e.name = dto.name;
        if (dto.descriptions != null && !dto.descriptions.isEmpty()) {
            e.description = dto.descriptions.get(0).desc;
        }
        if (dto.document != null) {
            e.documentName = dto.document.name;
            e.documentKey = dto.document.key;
        }
        return e;
    }
}
