package com.fizzycoyote.qusetroll.core.models.open5e.character_class.feature;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.fizzycoyote.qusetroll.core.models.open5e.character_class.CharacterClassEntity;

@Entity(
        tableName = "features",
        foreignKeys = @ForeignKey(
                entity = CharacterClassEntity.class,
                parentColumns = "class_key",
                childColumns = "class_key_ref",
                onDelete = ForeignKey.CASCADE
        ),
        indices = @Index("class_key_ref")
)
public class FeatureEntity {
    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "feature_key")
    public String key;

    @NonNull
    @ColumnInfo(name = "class_key_ref")
    public String classKey;

    public String name;
    public String desc;
    public String featureType;

    @ColumnInfo(name = "gained_at")
    public String gainedAtJson;

    @ColumnInfo(name = "table_data")
    public String tableDataJson;
}