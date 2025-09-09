package com.fizzycoyote.qusetroll.core.models.open5e.background.benefit;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

import com.fizzycoyote.qusetroll.core.models.open5e.background.BackgroundEntity;

@Entity(
        tableName = "benefits",
        foreignKeys = @ForeignKey(
                entity = BackgroundEntity.class,
                parentColumns = "key",
                childColumns = "backgroundKey",
                onDelete = ForeignKey.CASCADE
        )
)
public class BackgroundBenefitEntity {
    @PrimaryKey(autoGenerate = true) public long id;
    @NonNull public String backgroundKey;
    public String type;
    public String name;
    public String desc;
}
