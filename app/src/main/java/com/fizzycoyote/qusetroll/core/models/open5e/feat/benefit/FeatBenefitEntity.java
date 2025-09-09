package com.fizzycoyote.qusetroll.core.models.open5e.feat.benefit;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

import com.fizzycoyote.qusetroll.core.models.open5e.feat.FeatEntity;

@Entity(
        tableName = "feat_benefits",
        foreignKeys = @ForeignKey(
                entity = FeatEntity.class,
                parentColumns = "key",
                childColumns = "featKey",
                onDelete = ForeignKey.CASCADE
        )
)
public class FeatBenefitEntity {
    @PrimaryKey(autoGenerate = true) public long id;  // local id
    @NonNull public String featKey; // FK to feat entity

    public String desc;
}
