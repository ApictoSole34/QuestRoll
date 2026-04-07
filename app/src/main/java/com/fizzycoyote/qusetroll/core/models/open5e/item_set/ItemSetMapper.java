package com.fizzycoyote.qusetroll.core.models.open5e.item_set;

import com.fizzycoyote.qusetroll.core.models.open5e.item.ItemDto;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ItemSetMapper {
    public static ItemSetEntity dtoToEntity(ItemSetDto dto) {
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