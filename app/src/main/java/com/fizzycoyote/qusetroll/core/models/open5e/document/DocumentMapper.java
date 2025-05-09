package com.fizzycoyote.qusetroll.core.models.open5e.document;

import com.fizzycoyote.qusetroll.core.models.open5e.license.LicenseDto;

import java.util.ArrayList;
import java.util.List;

public class DocumentMapper {
    public static DocumentEntity dtoToEntity(DocumentDto dto) {
        DocumentEntity entity = new DocumentEntity();
        entity.key = dto.key;
        entity.url = dto.url;
        List<String> keys = new ArrayList<>();
        for (LicenseDto license : dto.licenses) {
            keys.add(license.key);
        }
        entity.licenses = keys;
        entity.publisher = dto.publisher;
        entity.gamesystem = dto.gamesystem;
        entity.name = dto.name;
        entity.desc = dto.desc;
        entity.author = dto.author;
        entity.publishedAt = dto.publishedAt;
        entity.permalink = dto.permalink;
        entity.distanceUnit = dto.distanceUnit;
        return entity;
    }
}
