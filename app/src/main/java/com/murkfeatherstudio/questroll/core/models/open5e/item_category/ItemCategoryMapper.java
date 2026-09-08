package com.murkfeatherstudio.questroll.core.models.open5e.item_category;

import com.murkfeatherstudio.questroll.core.database.base.Mapper;

public class ItemCategoryMapper implements Mapper<ItemCategoryEntity, ItemCategoryDto> {
    @Override
    public ItemCategoryEntity toEntity(ItemCategoryDto dto) {
        ItemCategoryEntity e = new ItemCategoryEntity();
        e.key = dto.key;
        e.name = dto.name;
        if (dto.document != null) {
            e.documentName = dto.document.name;
            e.documentKey = dto.document.key;
        }
        return e;
    }
}
