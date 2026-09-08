package com.murkfeatherstudio.questroll.core.models.open5e.service;

import com.murkfeatherstudio.questroll.core.database.base.Mapper;

public class ServiceMapper implements Mapper<ServiceEntity, ServiceDto> {
    @Override
    public ServiceEntity toEntity(ServiceDto dto) {
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
