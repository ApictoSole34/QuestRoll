package com.murkfeatherstudio.questroll.core.models.custom.custom_creature;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "custom_creatures")
public class CustomCreatureEntity {

    @PrimaryKey(autoGenerate = true)
    public long id;

    @NonNull
    public String name = "";

    @ColumnInfo(name = "cr_text") public String crText = "";
    @ColumnInfo(name = "cr_decimal") public float crDecimal = 0f;
    @ColumnInfo(name = "type_name") public String typeName = "";
    @ColumnInfo(name = "size_name") public String sizeName = "";
    public String alignment = "";

    @ColumnInfo(name = "armor_class") public int armorClass = 10;
    @ColumnInfo(name = "armor_detail") public String armorDetail = "";
    @ColumnInfo(name = "hit_points") public int hitPoints = 0;
    @ColumnInfo(name = "hit_dice") public String hitDice = "";

    @ColumnInfo(name = "speed_walk") public int speedWalk = 30;
    @ColumnInfo(name = "speed_fly") public int speedFly = 0;
    @ColumnInfo(name = "speed_swim") public int speedSwim = 0;
    @ColumnInfo(name = "speed_burrow") public int speedBurrow = 0;
    @ColumnInfo(name = "speed_climb") public int speedClimb = 0;
    @ColumnInfo(name = "speed_hover") public boolean speedHover = false;

    public int str = 10;
    public int dex = 10;
    public int con = 10;
    @ColumnInfo(name = "int_score") public int intScore = 10;
    public int wis = 10;
    public int cha = 10;

    @ColumnInfo(name = "saving_throws_json") public String savingThrowsJson = "";

    @ColumnInfo(name = "skill_bonuses_json") public String skillBonusesJson = "";

    @ColumnInfo(name = "darkvision_range") public int darkvisionRange = 0;
    @ColumnInfo(name = "blindsight_range") public int blindsightRange = 0;
    @ColumnInfo(name = "tremorsense_range") public int tremorsenseRange = 0;
    @ColumnInfo(name = "truesight_range") public int truesightRange = 0;
    @ColumnInfo(name = "passive_perception") public int passivePerception = 10;

    public String languages = "";

    @ColumnInfo(name = "damage_immunities") public String damageImmunities = "";
    @ColumnInfo(name = "damage_resistances") public String damageResistances = "";
    @ColumnInfo(name = "damage_vulnerabilities") public String damageVulnerabilities = "";
    @ColumnInfo(name = "condition_immunities") public String conditionImmunities = "";

    @ColumnInfo(name = "actions_json") public String actionsJson = "";
    @ColumnInfo(name = "traits_json") public String traitsJson = "";

    @ColumnInfo(name = "experience_points") public int experiencePoints = 0;
    @ColumnInfo(name = "initiative_bonus") public int initiativeBonus = 0;
}
