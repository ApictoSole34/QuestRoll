package com.fizzycoyote.qusetroll.core.models.custom.custom_ability;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "custom_abilities")
public class CustomAbilityEntity {
    @PrimaryKey(autoGenerate = true)
    public long id;

    @NonNull
    public String name = "";

    public String shortDesc;
    public String description;  // free-text Markdown

    /** epoch-millis, for sorting */
    public long createdAt;
}