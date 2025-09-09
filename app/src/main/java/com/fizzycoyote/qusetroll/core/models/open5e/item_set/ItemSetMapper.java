package com.fizzycoyote.qusetroll.core.models.open5e.item_set;

import com.fizzycoyote.qusetroll.core.models.open5e.item.ItemDto;

import java.util.ArrayList;
import java.util.List;

public class ItemSetMapper {
    public static ItemSetEntity dtoToEntity(ItemSetDto dto) {
        ItemSetEntity entity = new ItemSetEntity();
        entity.key = dto.key;
        entity.url = dto.url;
        entity.name = dto.name;
        entity.desc = dto.desc;
        entity.document = dto.document;
        List<String> keys = new ArrayList<>();
        for (ItemDto item : dto.items) {
            keys.add(item.key);
        }
        entity.itemKeys = keys;
        return entity;
    }
}
