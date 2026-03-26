package com.fizzycoyote.qusetroll.core.models.custom.custom_item;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "custom_items")
public class CustomItemEntity {

    @PrimaryKey(autoGenerate = true)
    public long id;

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

    @ColumnInfo(name = "is_magic_item")
    public boolean isMagicItem;

    @ColumnInfo(name = "weapon_json")
    public String weaponJson;
    @ColumnInfo(name = "armor_json")
    public String armorJson;

    public float weight;
    @ColumnInfo(name = "weight_unit")
    public String weightUnit;

    public float cost;

    @ColumnInfo(name = "requires_attunement")
    public boolean requiresAttunement;
    @ColumnInfo(name = "attunement_detail")
    public String attunementDetail;
}