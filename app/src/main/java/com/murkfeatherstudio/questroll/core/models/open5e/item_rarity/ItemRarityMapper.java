package com.murkfeatherstudio.questroll.core.models.open5e.item_rarity;

import com.murkfeatherstudio.questroll.core.database.base.Mapper;

public class ItemRarityMapper implements Mapper<ItemRarityEntity, ItemRarityDto> {
    @Override
    public ItemRarityEntity toEntity(ItemRarityDto dto) {
        ItemRarityEntity e = new ItemRarityEntity();
        e.key = dto.key;
        e.name = dto.name;
        e.url = dto.url;
        e.rank = dto.rank;
        return e;
    }
}
