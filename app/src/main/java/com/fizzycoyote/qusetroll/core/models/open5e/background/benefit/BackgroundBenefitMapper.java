package com.fizzycoyote.qusetroll.core.models.open5e.background.benefit;

import java.util.ArrayList;
import java.util.List;

public class BackgroundBenefitMapper {
    public static List<BackgroundBenefitEntity> dtoListToEntities(String bgKey, List<BackgroundBenefitDto> dtos) {
        List<BackgroundBenefitEntity> entities = new ArrayList<>();
        for (BackgroundBenefitDto dto : dtos) {
            BackgroundBenefitEntity entity = new BackgroundBenefitEntity();
            entity.backgroundKey = bgKey;
            entity.type = dto.type;
            entity.name = dto.name;
            entity.desc = dto.desc;
            entities.add(entity);
        }
        return entities;
    }
}
