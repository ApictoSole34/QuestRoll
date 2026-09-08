package com.murkfeatherstudio.questroll.core.models.character;

import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "inventory_items",
        foreignKeys = @ForeignKey(entity = CharacterEntity.class,
                parentColumns = "id",
                childColumns = "character_id",
                onDelete = ForeignKey.CASCADE),
        indices = @Index("character_id"))
public class InventoryItemEntity {
    @PrimaryKey(autoGenerate = true)
    public long id;

    @ColumnInfo(name = "character_id")
    public long characterId;

    @Nullable
    @ColumnInfo(name = "item_key")
    public String itemKey;

    @Nullable
    @ColumnInfo(name = "custom_name")
    public String customName;

    @Nullable
    @ColumnInfo(name = "custom_description")
    public String customDescription;

    @ColumnInfo(name = "custom_weight")
    public float customWeight;

    @ColumnInfo(name = "custom_cost")
    public float customCost;

    public int quantity;
    @ColumnInfo(name = "is_equipped")
    public boolean isEquipped;

    @Nullable
    public String slot;
}