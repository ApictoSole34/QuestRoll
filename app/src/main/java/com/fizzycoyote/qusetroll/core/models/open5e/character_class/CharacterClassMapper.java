package com.fizzycoyote.qusetroll.core.models.open5e.character_class;

import com.fizzycoyote.qusetroll.core.models.open5e.Converters;
import com.fizzycoyote.qusetroll.core.models.open5e.character_class.feature.FeatureDto;
import com.fizzycoyote.qusetroll.core.models.open5e.character_class.feature.FeatureEntity;
import com.fizzycoyote.qusetroll.core.models.open5e.character_class.hit_points.HitPointsEntity;
import com.fizzycoyote.qusetroll.core.models.open5e.character_class.saving_throw.SavingThrowEntity;

import java.util.List;
import java.util.stream.Collectors;

public class CharacterClassMapper {
    public static CharacterClassEntity toClassEntity(CharacterClassDto dto) {
        CharacterClassEntity entity = new CharacterClassEntity();
        entity.key = dto.key;
        entity.name = dto.name;
        entity.document = dto.document != null ? dto.document.key : null;
        entity.casterType = dto.casterType;
        entity.subclassOfKey = dto.subclassOf != null ? dto.subclassOf.key : null;
        return entity;
    }

    public static HitPointsEntity toHitPointsEntity(CharacterClassDto dto) {
        HitPointsEntity entity = new HitPointsEntity();
        entity.classKey = dto.key;

        if (dto.hitPoints != null) {
            entity.hitDice = dto.hitPoints.hitDice != null ?
                    dto.hitPoints.hitDice : "D?";
            entity.hitDiceName = dto.hitPoints.hitDiceName != null ?
                    dto.hitPoints.hitDiceName : "Standard";
            entity.at1stLevel = dto.hitPoints.at1stLevel != null ?
                    dto.hitPoints.at1stLevel : "N/A";
            entity.atHigherLevels = dto.hitPoints.atHigherLevels != null ?
                    dto.hitPoints.atHigherLevels : "N/A";
        } else {
            entity.hitDice = "D?";
            entity.hitDiceName = "N/A";
            entity.at1stLevel = "N/A";
            entity.atHigherLevels = "N/A";
        }

        return entity;
    }

    public static List<SavingThrowEntity> mapSavingThrows(CharacterClassDto dto) {
        return dto.savingThrows.stream()
                .map(savingThrowDto -> {
                    String ability = savingThrowDto.name;
                    return new SavingThrowEntity(dto.key, ability);
                })
                .collect(Collectors.toList());
    }

    public static List<FeatureEntity> toFeatureEntities(String classKey, List<FeatureDto> dtos) {
        return dtos.stream().map(dto -> {
            FeatureEntity entity = new FeatureEntity();
            entity.key = dto.key;
            entity.classKey = classKey;
            entity.name = dto.name;
            entity.desc = dto.desc;
            entity.featureType = dto.featureType;
            entity.gainedAtJson = Converters.gainedAtListToJson(dto.gainedAt);
            entity.tableDataJson = Converters.tableDataListToJson(dto.tableData);
            return entity;
        }).collect(Collectors.toList());
    }
}
