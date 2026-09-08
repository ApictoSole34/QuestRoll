package com.murkfeatherstudio.questroll.core.models.open5e.language;

import com.murkfeatherstudio.questroll.core.database.base.Mapper;

public class LanguageMapper implements Mapper<LanguageEntity, LanguageDto> {
    @Override
    public LanguageEntity toEntity(LanguageDto dto) {
        LanguageEntity entity = new LanguageEntity();
        entity.key = dto.key;
        entity.url = dto.url;
        entity.document = dto.document != null ? dto.document.key : null;
        entity.name = dto.name;
        entity.desc = dto.desc;
        entity.isExotic = dto.isExotic;
        entity.isSecret = dto.isSecret;
        entity.scriptLanguage = dto.scriptLanguage;
        return entity;
    }
}
