package com.fizzycoyote.qusetroll.core.models.open5e.character_class;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import org.jspecify.annotations.Nullable;

@Entity(tableName = "classes")
public class CharacterClassEntity {
    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "class_key")
    public String key;

    public String name;
    public String document;@Nullable
    public String casterType;
    //@ColumnInfo(name = "subclass_of") @Nullable public String subclassOf;
    @ColumnInfo(name = "subclass_of_key")
    @Nullable
    public String subclassOfKey;
}