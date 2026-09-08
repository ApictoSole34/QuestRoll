package com.murkfeatherstudio.questroll.core.models.open5e.item_set;

import com.murkfeatherstudio.questroll.core.database.base.Mapper;

import java.util.stream.Collectors;

public class ItemSetMapper implements Mapper<ItemSetEntity, ItemSetDto> {
    @Override
    public ItemSetEntity toEntity(ItemSetDto dto) {
        ItemSetEntity e = new ItemSetEntity();
        e.key = dto.key;
        e.name = dto.name;
        e.desc = dto.desc;
        e.documentUrl = dto.document;
        if (dto.items != null) {
            e.itemKeys = dto.items.stream()
                    .map(i -> i.key)
                    .collect(Collectors.toList());
        }
        return e;
    }
}
