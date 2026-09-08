package com.murkfeatherstudio.questroll.core.models.open5e.document;

import com.murkfeatherstudio.questroll.core.database.base.Mapper;
import com.murkfeatherstudio.questroll.core.models.open5e.license.LicenseDto;

import java.util.ArrayList;
import java.util.List;

public class DocumentMapper implements Mapper<DocumentEntity, DocumentDto> {
    @Override
    public DocumentEntity toEntity(DocumentDto dto) {
        DocumentEntity entity = new DocumentEntity();
        entity.key = dto.key;
        entity.url = dto.url;
        List<String> keys = new ArrayList<>();
        if (dto.licenses != null) {
            for (LicenseDto license : dto.licenses) {
                keys.add(license.key);
            }
        }
        entity.publisher = dto.publisher != null ? dto.publisher.key : null;
        entity.gamesystem = dto.gamesystem != null ? dto.gamesystem.key : null;
        entity.licenses = keys;
        entity.name = dto.name;
        entity.desc = dto.desc;
        entity.author = dto.author;
        entity.publishedAt = dto.publishedAt;
        entity.permalink = dto.permalink;
        entity.distanceUnit = dto.distanceUnit;
        return entity;
    }
}
