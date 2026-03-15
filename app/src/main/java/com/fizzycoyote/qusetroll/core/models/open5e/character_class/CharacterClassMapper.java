package com.fizzycoyote.qusetroll.core.models.open5e.character_class;

import com.fizzycoyote.qusetroll.core.models.open5e.Converters;
import com.fizzycoyote.qusetroll.core.models.open5e.character_class.feature.FeatureDto;
import com.fizzycoyote.qusetroll.core.models.open5e.character_class.feature.FeatureEntity;
import com.fizzycoyote.qusetroll.core.models.open5e.character_class.gained_at.GainedAt;
import com.fizzycoyote.qusetroll.core.models.open5e.character_class.hit_points.HitPointsEntity;
import com.fizzycoyote.qusetroll.core.models.open5e.character_class.saving_throw.SavingThrowEntity;
import com.fizzycoyote.qusetroll.core.models.open5e.character_class.table_data.TableData;

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

        if (dto.hitPoints != null) {
            entity.hitDice = dto.hitPoints.hitDice;
            entity.hitDiceName = dto.hitPoints.hitDiceName;
            entity.hitPointsAt1stLevel = dto.hitPoints.at1stLevel;
            entity.hitPointsAtHigherLevels = dto.hitPoints.atHigherLevels;
        }

        return entity;
    }

    public static HitPointsEntity toHitPointsEntity(CharacterClassDto dto) {
        HitPointsEntity entity = new HitPointsEntity();
        entity.classKey = dto.key;

        if (dto.hitPoints != null) {
            entity.hitDice = dto.hitPoints.hitDice;
            entity.hitDiceName = dto.hitPoints.hitDiceName;
            entity.at1stLevel = dto.hitPoints.at1stLevel;
            entity.atHigherLevels = dto.hitPoints.atHigherLevels;
        }

        return entity;
    }

    public static List<SavingThrowEntity> mapSavingThrows(CharacterClassDto dto) {
        return dto.savingThrows.stream()
                .map(savingThrowDto -> {
                    SavingThrowEntity entity = new SavingThrowEntity();
                    entity.classKey = dto.key;
                    entity.abilityKey = extractKeyFromUrl(savingThrowDto.url);
                    entity.abilityName = savingThrowDto.name;
                    return entity;
                })
                .collect(Collectors.toList());
    }

    private static String extractKeyFromUrl(String url) {
        if (url == null) return null;
        String u = url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
        String[] parts = u.split("/");
        return parts.length > 0 ? parts[parts.length - 1] : null;
    }

    public static List<FeatureEntity> toFeatureEntities(String classKey, List<FeatureDto> dtos) {
        return dtos.stream().map(dto -> {
            FeatureEntity entity = new FeatureEntity();
            entity.key = dto.key;
            entity.classKey = classKey;
            entity.name = dto.name;
            entity.desc = dto.desc;
            entity.featureType = dto.featureType;

            if (dto.gainedAt != null) {
                entity.gainedAt = dto.gainedAt.stream()
                        .map(gainedAtDto -> {
                            GainedAt gainedAt = new GainedAt();
                            gainedAt.level = gainedAtDto.level;
                            gainedAt.detail = gainedAtDto.detail;
                            return gainedAt;
                        })
                        .collect(Collectors.toList());
            }

            if (dto.tableData != null) {
                entity.tableData = dto.tableData.stream()
                        .map(tableDataDto -> {
                            TableData tableData = new TableData();
                            tableData.level = tableDataDto.level;
                            tableData.columnValue = tableDataDto.columnValue;
                            return tableData;
                        })
                        .collect(Collectors.toList());
            }

            return entity;
        }).collect(Collectors.toList());
    }
}