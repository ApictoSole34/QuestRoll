package com.fizzycoyote.qusetroll.core.models.open5e.item_rarity;

public class ItemRarityMapper {
    public static ItemRarityEntity dtoToEntity(ItemRarityDto dto) {
        ItemRarityEntity e = new ItemRarityEntity();
        e.key = dto.key;
        e.name = dto.name;
        e.url = dto.url;
        e.rank = dto.rank;
        return e;
    }
}
