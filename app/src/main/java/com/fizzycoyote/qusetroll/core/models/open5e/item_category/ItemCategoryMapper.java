package com.fizzycoyote.qusetroll.core.models.open5e.item_category;

public class ItemCategoryMapper {
    public static ItemCategoryEntity dtoToEntity(ItemCategoryDto dto) {
        ItemCategoryEntity entity = new ItemCategoryEntity();
        entity.key = dto.key;
        entity.url = dto.url;
        entity.name = dto.name;
        entity.document = dto.document;
        return entity;
    }
}
