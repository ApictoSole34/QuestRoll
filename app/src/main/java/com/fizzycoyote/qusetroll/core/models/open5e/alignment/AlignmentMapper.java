package com.fizzycoyote.qusetroll.core.models.open5e.alignment;

public class AlignmentMapper {
    public static AlignmentEntity dtoToEntity(AlignmentDto dto) {
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