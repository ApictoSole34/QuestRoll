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
}