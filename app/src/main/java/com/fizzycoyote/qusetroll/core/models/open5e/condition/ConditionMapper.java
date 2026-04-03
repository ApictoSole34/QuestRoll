package com.fizzycoyote.qusetroll.core.models.open5e.condition;

public class ConditionMapper {
    public static ConditionEntity dtoToEntity(ConditionDto dto) {
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