package com.murkfeatherstudio.questroll.core.models.open5e.item;

import com.murkfeatherstudio.questroll.core.database.base.Mapper;
import com.google.gson.Gson;

public class ItemMapper implements Mapper<ItemEntity, ItemDto> {
    private final Gson gson = new Gson();

    @Override
    public ItemEntity toEntity(ItemDto dto) {
        ItemEntity e = new ItemEntity();
        e.key = dto.key;
        e.name = dto.name;
        e.desc = dto.desc;
        if (dto.category != null) {
            e.categoryName = dto.category.name;
            e.categoryKey = dto.category.key;
        }
        if (dto.rarity != null) {
            e.rarityName = dto.rarity.name;
            e.rarityKey = dto.rarity.key;
            e.rarityRank = dto.rarity.rank;
        }
        e.isMagicItem = dto.isMagicItem;
        if (dto.weapon != null) e.weaponJson = gson.toJson(dto.weapon);
        if (dto.armor != null) e.armorJson = gson.toJson(dto.armor);
        if (dto.size != null) {
            e.sizeName = dto.size.name;
            e.sizeKey = dto.size.key;
        }
        try { e.weight = Float.parseFloat(dto.weight); } catch (Exception ex) { e.weight = 0f; }
        e.weightUnit = dto.weightUnit;
        try { e.cost = Float.parseFloat(dto.cost); } catch (Exception ex) { e.cost = 0f; }
        e.requiresAttunement = dto.requiresAttunement;
        e.attunementDetail = dto.attunementDetail;
        if (dto.document != null) {
            e.documentName = dto.document.name;
            e.documentKey = dto.document.key;
        }
        return e;
    }
}
