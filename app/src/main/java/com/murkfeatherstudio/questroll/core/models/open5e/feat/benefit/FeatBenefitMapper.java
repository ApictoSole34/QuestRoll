package com.murkfeatherstudio.questroll.core.models.open5e.feat.benefit;

import com.murkfeatherstudio.questroll.core.database.base.Mapper;

import java.util.ArrayList;
import java.util.List;

public class FeatBenefitMapper implements Mapper<FeatBenefitEntity, FeatBenefitDto> {

    @Override
    public FeatBenefitEntity toEntity(FeatBenefitDto dto) {
        FeatBenefitEntity entity = new FeatBenefitEntity();
        entity.desc = dto.desc;
        return entity;
    }

    public List<FeatBenefitEntity> dtosToEntities(String featKey, List<FeatBenefitDto> dtos) {
        List<FeatBenefitEntity> entities = new ArrayList<>();
        if (dtos == null) return entities;
        for (FeatBenefitDto dto : dtos) {
            FeatBenefitEntity entity = toEntity(dto);
            entity.featKey = featKey;
            entities.add(entity);
        }
        return entities;
    }
}
