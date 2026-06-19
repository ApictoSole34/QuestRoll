package com.fizzycoyote.qusetroll.core.models.open5e.creature;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "creatures", indices = {
        @Index("name"),
        @Index("challenge_rating_decimal"),
        @Index("type_key"),
        @Index("document_name")
})
public class CreatureEntity {

    @PrimaryKey
    @NonNull
    public String key;

    public String name;

    @ColumnInfo(name = "type_name") public String typeName;
    @ColumnInfo(name = "type_key") public String typeKey;
    @ColumnInfo(name = "size_name") public String sizeName;
    @ColumnInfo(name = "size_key") public String sizeKey;



    public String alignment;
    public String category;

    @ColumnInfo(name = "challenge_rating_decimal") public float challengeRatingDecimal;
    @ColumnInfo(name = "challenge_rating_text") public String challengeRatingText;

    @ColumnInfo(name = "armor_class") public int armorClass;
    @ColumnInfo(name = "armor_detail") public String armorDetail;
    @ColumnInfo(name = "hit_points") public int hitPoints;
    @ColumnInfo(name = "hit_dice") public String hitDice;
    @ColumnInfo(name = "experience_points") public int experiencePoints;
    @ColumnInfo(name = "initiative_bonus") public int initiativeBonus;
    @ColumnInfo(name = "passive_perception") public int passivePerception;

    @ColumnInfo(name = "speed_json") public String speedJson;

    @ColumnInfo(name = "str") public int str;
    @ColumnInfo(name = "dex") public int dex;
    @ColumnInfo(name = "con") public int con;
    @ColumnInfo(name = "int_score") public int intScore;
    @ColumnInfo(name = "wis") public int wis;
    @ColumnInfo(name = "cha") public int cha;

    @ColumnInfo(name = "str_mod") public int strMod;
    @ColumnInfo(name = "dex_mod") public int dexMod;
    @ColumnInfo(name = "con_mod") public int conMod;
    @ColumnInfo(name = "int_mod") public int intMod;
    @ColumnInfo(name = "wis_mod") public int wisMod;
    @ColumnInfo(name = "cha_mod") public int chaMod;

    @ColumnInfo(name = "saving_throws_json") public String savingThrowsJson;

    @ColumnInfo(name = "skill_bonuses_json") public String skillBonusesJson;

    @ColumnInfo(name = "darkvision_range") public Float darkvisionRange;
    @ColumnInfo(name = "blindsight_range") public Float blindsightRange;
    @ColumnInfo(name = "tremorsense_range") public Float tremorsenseRange;
    @ColumnInfo(name = "truesight_range") public Float truesightRange;

    @ColumnInfo(name = "languages") public String languages;

    @ColumnInfo(name = "damage_immunities") public String damageImmunities;
    @ColumnInfo(name = "damage_resistances") public String damageResistances;
    @ColumnInfo(name = "damage_vulnerabilities") public String damageVulnerabilities;
    @ColumnInfo(name = "condition_immunities") public String conditionImmunities;

    @ColumnInfo(name = "actions_json") public String actionsJson;

    @ColumnInfo(name = "traits_json") public String traitsJson;

    @ColumnInfo(name = "document_name") public String documentName;
    @ColumnInfo(name = "document_key") public String documentKey;
}