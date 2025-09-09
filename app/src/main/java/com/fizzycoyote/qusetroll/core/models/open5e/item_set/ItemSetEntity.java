package com.fizzycoyote.qusetroll.core.models.open5e.item_set;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.fizzycoyote.qusetroll.core.models.open5e.Converters;

import java.util.List;

@Entity(tableName = "item_sets")
@TypeConverters(Converters.class)
public class ItemSetEntity {
    @PrimaryKey @NonNull public String key;
    public String url;
    public String name;
    @Nullable public String desc;
    public String document;
    public List<String> itemKeys; // list of ItemDto.key values
}
