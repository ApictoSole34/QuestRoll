package com.fizzycoyote.qusetroll.core.models.open5e.creature;

import com.fizzycoyote.qusetroll.core.models.open5e.document.DocumentDto;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class CreatureDto implements Serializable {
    @SerializedName("key") public String key;
    @SerializedName("name") public String name;
    @SerializedName("document") public DocumentDto document;
    @SerializedName("type") public CreatureTypeRefDto type;
    @SerializedName("size") public CreatureSizeRefDto size;
    @SerializedName("challenge_rating") public Float challengeRating;
    // old    @SerializedName("challenge_rating_decimal") public String challengeRatingDecimal;
// old   @SerializedName("challenge_rating_text") public String challengeRatingText;
    @SerializedName("alignment") public String alignment;
    @SerializedName("category") public String category;
    @SerializedName("armor_class") public int armorClass;
    @SerializedName("armor_detail") public String armorDetail;
    @SerializedName("hit_points") public int hitPoints;
    @SerializedName("hit_dice") public String hitDice;
    @SerializedName("experience_points") public int experiencePoints;
    @SerializedName("initiative_bonus") public int initiativeBonus;
    @SerializedName("passive_perception") public int passivePerception;
    @SerializedName("speed") public CreatureSpeedDto speed;
    @SerializedName("ability_scores") public AbilityScoresDto abilityScores;
    @SerializedName("modifiers") public AbilityScoresDto modifiers;
    @SerializedName("saving_throws") public Map<String, Integer> savingThrows;
    @SerializedName("skill_bonuses") public Map<String, Integer> skillBonuses;
    @SerializedName("darkvision_range") public Float darkvisionRange;
    @SerializedName("blindsight_range") public Float blindsightRange;
    @SerializedName("tremorsense_range") public Float tremorsenseRange;
    @SerializedName("truesight_range") public Float truesightRange;
    @SerializedName("languages") public CreatureLanguagesDto languages;
    @SerializedName("resistances_and_immunities") public ResistancesDto resistancesAndImmunities;
    @SerializedName("actions") public List<CreatureActionDto> actions;
    @SerializedName("traits") public List<CreatureTraitDto> traits;

    public static class CreatureTypeRefDto implements Serializable {
        @SerializedName("name") public String name;
        @SerializedName("key") public String key;
    }

    public static class CreatureSizeRefDto implements Serializable {
        @SerializedName("name") public String name;
        @SerializedName("key") public String key;
    }

    public static class CreatureSpeedDto implements Serializable {
        @SerializedName("walk") public Float walk;
        @SerializedName("fly") public Float fly;
        @SerializedName("swim") public Float swim;
        @SerializedName("burrow") public Float burrow;
        @SerializedName("climb") public Float climb;
        @SerializedName("hover") public Boolean hover;
        @SerializedName("unit") public String unit;
    }

    public static class AbilityScoresDto implements Serializable {
        @SerializedName("strength") public int strength;
        @SerializedName("dexterity") public int dexterity;
        @SerializedName("constitution") public int constitution;
        @SerializedName("intelligence") public int intelligence;
        @SerializedName("wisdom") public int wisdom;
        @SerializedName("charisma") public int charisma;
    }

    public static class CreatureLanguagesDto implements Serializable {
        @SerializedName("as_string") public String asString;
    }

    public static class ResistancesDto implements Serializable {
        @SerializedName("damage_immunities_display") public String damageImmunitiesDisplay;
        @SerializedName("damage_resistances_display") public String damageResistancesDisplay;
        @SerializedName("damage_vulnerabilities_display") public String damageVulnerabilitiesDisplay;
        @SerializedName("condition_immunities_display") public String conditionImmunitiesDisplay;
    }

    public static class CreatureActionDto implements Serializable {
        @SerializedName("name") public String name;
        @SerializedName("desc") public String desc;
        @SerializedName("action_type") public String actionType;
        @SerializedName("legendary_action_cost") public Integer legendaryActionCost;
        @SerializedName("order_in_statblock") public int orderInStatblock;
        @SerializedName("attacks") public List<CreatureAttackDto> attacks;
    }

    public static class CreatureAttackDto implements Serializable {
        @SerializedName("name") public String name;
        @SerializedName("attack_type") public String attackType;
        @SerializedName("to_hit_mod") public Integer toHitMod;
        @SerializedName("reach") public Float reach;
        @SerializedName("range") public Float range;
        @SerializedName("damage_die_count") public Integer damageDieCount;
        @SerializedName("damage_die_type") public String damageDieType;
        @SerializedName("damage_bonus") public Integer damageBonus;
    }

    public static class CreatureTraitDto implements Serializable {
        @SerializedName("name") public String name;
        @SerializedName("desc") public String desc;
    }
}