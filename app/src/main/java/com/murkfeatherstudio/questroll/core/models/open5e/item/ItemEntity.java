package com.murkfeatherstudio.questroll.core.models.open5e.item;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "items", indices = {
        @Index("name"),
        @Index("category_name"),
        @Index("document_name")
})
public class ItemEntity {

    @PrimaryKey
    @NonNull
    public String key;

    public String name;
    public String desc;

    @ColumnInfo(name = "category_name")
    public String categoryName;
    @ColumnInfo(name = "category_key")
    public String categoryKey;

    @ColumnInfo(name = "rarity_name")
    public String rarityName;
    @ColumnInfo(name = "rarity_key")
    public String rarityKey;
    @ColumnInfo(name = "rarity_rank")
    public int rarityRank;

    @ColumnInfo(name = "is_magic_item")
    public boolean isMagicItem;

    @ColumnInfo(name = "weapon_json")
    public String weaponJson;
    @ColumnInfo(name = "armor_json")
    public String armorJson;

    @ColumnInfo(name = "size_name")
    public String sizeName;
    @ColumnInfo(name = "size_key")
    public String sizeKey;

    public float weight;
    @ColumnInfo(name = "weight_unit")
    public String weightUnit;

    public float cost;

    @ColumnInfo(name = "requires_attunement")
    public boolean requiresAttunement;
    @ColumnInfo(name = "attunement_detail")
    public String attunementDetail;

    @ColumnInfo(name = "document_name")
    public String documentName;
    @ColumnInfo(name = "document_key")
    public String documentKey;
}