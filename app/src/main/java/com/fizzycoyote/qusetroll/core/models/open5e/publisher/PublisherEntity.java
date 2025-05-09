package com.fizzycoyote.qusetroll.core.models.open5e.publisher;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "publishers")
public class PublisherEntity {
    @PrimaryKey @NonNull public String key;
    public String url;
    public String name;
}
