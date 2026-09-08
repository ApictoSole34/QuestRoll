package com.murkfeatherstudio.questroll.core.models.open5e.background;

import com.murkfeatherstudio.questroll.core.database.base.Mapper;
import com.google.gson.Gson;

public class BackgroundMapper implements Mapper<BackgroundEntity, BackgroundDto> {
    private final Gson gson = new Gson();

    @Override
    public BackgroundEntity toEntity(BackgroundDto dto) {
        BackgroundEntity e = new BackgroundEntity();
        e.key = dto.key;
        e.name = dto.name;
        e.desc = dto.desc;
        if (dto.benefits != null) {
            e.benefitsJson = gson.toJson(dto.benefits);
        }
        if (dto.document != null) {
            e.documentName = dto.document.name;
            e.documentKey = dto.document.key;
        }
        return e;
    }
}
