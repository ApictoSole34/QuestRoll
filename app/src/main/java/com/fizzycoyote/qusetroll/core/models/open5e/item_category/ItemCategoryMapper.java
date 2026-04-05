package com.fizzycoyote.qusetroll.core.models.open5e.item_category;

public class ItemCategoryMapper {
    public static ItemCategoryEntity dtoToEntity(ItemCategoryDto dto) {
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

