package com.fizzycoyote.qusetroll.core.models.open5e.feat.benefit;

import java.util.ArrayList;
import java.util.List;

public class FeatBenefitMapper {
    public static List<FeatBenefitEntity> dtoToEntity(String featKey, List<FeatBenefitDto> dtos) {
        List<FeatBenefitEntity> entities = new ArrayList<>();
        for (FeatBenefitDto dto : dtos) {
            FeatBenefitEntity entity = new FeatBenefitEntity();
            entity.featKey = featKey;
            entity.desc = dto.desc;
            entities.add(entity);
        }
        return entities;
    }
}
