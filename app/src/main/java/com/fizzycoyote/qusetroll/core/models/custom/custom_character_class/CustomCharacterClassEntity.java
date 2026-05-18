package com.fizzycoyote.qusetroll.core.models.custom.custom_character_class;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.fizzycoyote.qusetroll.core.models.open5e.Converters;

import java.util.List;

@Entity(tableName = "custom_character_classes")
public class CustomCharacterClassEntity {
    @PrimaryKey(autoGenerate = true)
    public long id;

    @NonNull
    public String name;
    public String hitDice;
    public String description;

    @ColumnInfo(name = "subclass_of")
    @Nullable
    public String subclassOf;

    @ColumnInfo(name = "caster_type")
    @Nullable
    public String casterType;

    @TypeConverters(Converters.class)
    public List<String> savingThrows;

    @ColumnInfo(name = "game_system")
    public String gameSystem = "5e-2014";

    @ColumnInfo(name = "spellcasting_ability")
    public String spellcastingAbility = "NONE";

    @ColumnInfo(name = "starting_gold_dice")
    public String startingGoldDice = "5d4";

    @ColumnInfo(name = "skill_choices_count")
    public int skillChoicesCount = 0;

    @ColumnInfo(name = "skill_options_json")
    public String skillOptionsJson = "[]";

    @ColumnInfo(name = "equipment_description")
    public String equipmentDescription = "";

    @ColumnInfo(name = "resource_data")
    public String resourceData;

    @ColumnInfo(name = "custom_data")
    public String customData;

    @ColumnInfo(name = "language_keys_json")
    public String languageKeysJson = "[]";

    @ColumnInfo(name = "language_choices")
    public int languageChoices = 0;
}