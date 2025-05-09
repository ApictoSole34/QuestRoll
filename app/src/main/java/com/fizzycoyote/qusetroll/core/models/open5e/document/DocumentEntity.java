package com.fizzycoyote.qusetroll.core.models.open5e.document;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.fizzycoyote.qusetroll.core.models.open5e.Converters;

import java.util.List;

@Entity(tableName = "documents")
@TypeConverters({Converters.class})
public class DocumentEntity {
    @PrimaryKey @NonNull public String key;
    public String url;
    public List<String> licenses;
    @Nullable public String publisher;
    @Nullable public String gamesystem;
    public String name;
    @Nullable public String desc;
    public String author;
    public String publishedAt;
    public String permalink;
    public String distanceUnit;
}
