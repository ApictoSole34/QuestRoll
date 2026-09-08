package com.murkfeatherstudio.questroll.core.models.open5e.alignment;

import com.murkfeatherstudio.questroll.core.database.base.Mapper;

public class AlignmentMapper implements Mapper<AlignmentEntity, AlignmentDto> {

    @Override
    public AlignmentEntity toEntity(AlignmentDto dto) {
        AlignmentEntity e = new AlignmentEntity();
        e.key = dto.key;
        e.morality = dto.morality;
        e.societalAttitude = dto.societalAttitude;
        e.shortName = dto.shortName;
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
