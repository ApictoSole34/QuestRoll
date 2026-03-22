package com.fizzycoyote.qusetroll.core.models.open5e.background;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "backgrounds", indices = {
        @Index("name"),
        @Index("document_name")
})
public class BackgroundEntity {

    @PrimaryKey
    @NonNull
    public String key;

    public String name;
    public String desc;

    @ColumnInfo(name = "benefits_json") public String benefitsJson;
    @ColumnInfo(name = "document_name") public String documentName;
    @ColumnInfo(name = "document_key") public String documentKey;
}
