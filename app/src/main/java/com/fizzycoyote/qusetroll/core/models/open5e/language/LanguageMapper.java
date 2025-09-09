package com.fizzycoyote.qusetroll.core.models.open5e.language;

public class LanguageMapper {
    public static LanguageEntity dtoToEntity(LanguageDto dto) {
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
