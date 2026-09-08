package com.murkfeatherstudio.questroll.core.models.open5e.item;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class ItemDto implements Serializable {

    @SerializedName("key")
    public String key;

    @SerializedName("name")
    public String name;

    @SerializedName("desc")
    public String desc;

    @SerializedName("category")
    public CategoryDto category;

    @SerializedName("rarity")
    public RarityDto rarity;

    @SerializedName("is_magic_item")
    public boolean isMagicItem;

    @SerializedName("weapon")
    public WeaponEmbedDto weapon;

    @SerializedName("armor")
    public ArmorEmbedDto armor;

    @SerializedName("size")
    public SizeDto size;

    @SerializedName("weight")
    public String weight;

    @SerializedName("weight_unit")
    public String weightUnit;

    @SerializedName("cost")
    public String cost;

    @SerializedName("requires_attunement")
    public boolean requiresAttunement;

    @SerializedName("attunement_detail")
    public String attunementDetail;

    @SerializedName("document")
    public DocumentDto document;

    public static class CategoryDto implements Serializable {
        @SerializedName("name")
        public String name;
        @SerializedName("key")
        public String key;
        @SerializedName("url")
        public String url;
    }

    public static class RarityDto implements Serializable {
        @SerializedName("name")
        public String name;
        @SerializedName("key")
        public String key;
        @SerializedName("url")
        public String url;
        @SerializedName("rank")
        public int rank;
    }

    public static class SizeDto implements Serializable {
        @SerializedName("name")
        public String name;
        @SerializedName("key")
        public String key;
        @SerializedName("url")
        public String url;
    }

    public static class DocumentDto implements Serializable {
        @SerializedName("name")
        public String name;
        @SerializedName("key")
        public String key;
        @SerializedName("type")
        public String type;
        @SerializedName("display_name")
        public String displayName;
        @SerializedName("publisher")
        public PublisherDto publisher;
        @SerializedName("gamesystem")
        public GameSystemDto gamesystem;
        @SerializedName("permalink")
        public String permalink;

        public static class PublisherDto implements Serializable {
            @SerializedName("name")
            public String name;
            @SerializedName("key")
            public String key;
            @SerializedName("url")
            public String url;
        }

        public static class GameSystemDto implements Serializable {
            @SerializedName("name")
            public String name;
            @SerializedName("key")
            public String key;
            @SerializedName("url")
            public String url;
        }
    }

    public static class WeaponEmbedDto implements Serializable {
        @SerializedName("name")
        public String name;
        @SerializedName("key")
        public String key;
        @SerializedName("url")
        public String url;
        @SerializedName("damage_dice")
        public String damageDice;
        @SerializedName("damage_type")
        public DamageTypeDto damageType;
        @SerializedName("range")
        public float range;
        @SerializedName("long_range")
        public float longRange;
        @SerializedName("is_simple")
        public boolean isSimple;
        @SerializedName("is_improvised")
        public boolean isImprovised;
        @SerializedName("properties")
        public List<WeaponPropertyDto> properties;

        public static class DamageTypeDto implements Serializable {
            @SerializedName("name")
            public String name;
            @SerializedName("key")
            public String key;
        }

        public static class WeaponPropertyDto implements Serializable {
            @SerializedName("property")
            public PropertyDetailDto property;
            @SerializedName("detail")
            public String detail;

            public static class PropertyDetailDto implements Serializable {
                @SerializedName("name")
                public String name;
                @SerializedName("type")
                public String type;
                @SerializedName("desc")
                public String desc;
            }
        }
    }

    public static class ArmorEmbedDto implements Serializable {
        @SerializedName("name")
        public String name;
        @SerializedName("key")
        public String key;
        @SerializedName("url")
        public String url;
        @SerializedName("ac_base")
        public int acBase;
        @SerializedName("ac_display")
        public String acDisplay;
        @SerializedName("ac_add_dexmod")
        public boolean acAddDexmod;
        @SerializedName("ac_cap_dexmod")
        public Integer acCapDexmod;
        @SerializedName("grants_stealth_disadvantage")
        public boolean grantsStealthDisadvantage;
        @SerializedName("strength_score_required")
        public Integer strengthScoreRequired;
    }
}