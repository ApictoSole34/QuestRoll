package com.fizzycoyote.qusetroll.core.models.open5e.service;

public class ServiceMapper {
    public static ServiceEntity dtoToEntity(ServiceDto dto) {
        ServiceEntity e = new ServiceEntity();
        e.key = dto.key;
        e.name = dto.name;
        e.desc = dto.desc;
        e.cost = dto.cost;
        e.detail = dto.detail;
        e.url = dto.url;
        if (dto.document != null) {
            e.documentName = dto.document.name;
            e.documentKey = dto.document.key;
        }
        return e;
    }
}