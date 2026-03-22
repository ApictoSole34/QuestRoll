package com.fizzycoyote.qusetroll.core.models.open5e.background;

import com.google.gson.Gson;

public class BackgroundMapper {
    public static BackgroundEntity dtoToEntity(BackgroundDto dto) {
        BackgroundEntity e = new BackgroundEntity();
        e.key = dto.key;
        e.name = dto.name;
        e.desc = dto.desc;
        if (dto.benefits != null) {
            e.benefitsJson = new Gson().toJson(dto.benefits);
        }
        if (dto.document != null) {
            e.documentName = dto.document.name;
            e.documentKey = dto.document.key;
        }
        return e;
    }
}