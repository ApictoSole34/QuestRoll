package com.fizzycoyote.qusetroll.core.models.custom.custom_character_class.custom_feature;


import android.os.Parcel;
import android.os.Parcelable;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.fizzycoyote.qusetroll.core.models.custom.CustomConverters;
import com.fizzycoyote.qusetroll.core.models.custom.custom_character_class.CustomCharacterClassEntity;
import com.fizzycoyote.qusetroll.core.models.custom.custom_character_class.custom_gained_at.CustomGainedAt;
import com.fizzycoyote.qusetroll.core.models.custom.custom_character_class.custom_table_data.CustomTableData;
import com.fizzycoyote.qusetroll.core.models.open5e.Converters;
import com.fizzycoyote.qusetroll.core.models.open5e.character_class.gained_at.GainedAtDto;
import com.fizzycoyote.qusetroll.core.models.open5e.character_class.table_data.TableDataDto;

import java.util.ArrayList;
import java.util.List;

@Entity(
        tableName = "custom_features",
        foreignKeys = @ForeignKey(
                entity = CustomCharacterClassEntity.class,
                parentColumns = "id",
                childColumns = "class_id",
                onDelete = ForeignKey.CASCADE
        ),
        indices = {@Index("class_id")}
)
public class CustomFeatureEntity implements Parcelable {
    @PrimaryKey(autoGenerate = true)
    public long id;

    @ColumnInfo(name = "class_id")
    public long classId;

    public String name;
    public String description;
    public String type;

    @TypeConverters(CustomConverters.class)
    public List<CustomGainedAt> customGainedAt = new ArrayList<>();

    @TypeConverters(CustomConverters.class)
    public List<CustomTableData> customTableData = new ArrayList<>();

    protected CustomFeatureEntity(Parcel in) {
        id = in.readLong();
        classId = in.readLong();
        name = in.readString();
        description = in.readString();
        type = in.readString();
        customGainedAt = in.createTypedArrayList(CustomGainedAt.CREATOR);
        customTableData = in.createTypedArrayList(CustomTableData.CREATOR);
    }

    public CustomFeatureEntity() {}

    public static final Creator<CustomFeatureEntity> CREATOR = new Creator<CustomFeatureEntity>() {
        @Override
        public CustomFeatureEntity createFromParcel(Parcel in) {
            return new CustomFeatureEntity(in);
        }

        @Override
        public CustomFeatureEntity[] newArray(int size) {
            return new CustomFeatureEntity[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeLong(classId);
        dest.writeString(name);
        dest.writeString(description);
        dest.writeString(type);
        dest.writeTypedList(customGainedAt);
        dest.writeTypedList(customTableData);
    }
}