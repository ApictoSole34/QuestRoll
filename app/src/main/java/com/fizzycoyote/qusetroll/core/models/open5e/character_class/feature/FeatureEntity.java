package com.fizzycoyote.qusetroll.core.models.open5e.character_class.feature;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.fizzycoyote.qusetroll.core.models.open5e.character_class.CharacterClassEntity;
import com.fizzycoyote.qusetroll.core.models.open5e.character_class.gained_at.GainedAt;
import com.fizzycoyote.qusetroll.core.models.open5e.character_class.gained_at.GainedAtListConverter;
import com.fizzycoyote.qusetroll.core.models.open5e.character_class.table_data.TableData;
import com.fizzycoyote.qusetroll.core.models.open5e.character_class.table_data.TableDataListConverter;

import java.util.List;

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
    public List<GainedAt> gainedAt;
    public List<TableData> tableData;
}

