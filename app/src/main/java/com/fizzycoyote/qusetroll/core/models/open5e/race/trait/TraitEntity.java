package com.fizzycoyote.qusetroll.core.models.open5e.race.trait;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

import com.fizzycoyote.qusetroll.core.models.open5e.race.RaceEntity;


@Entity(
        tableName = "traits",
        foreignKeys = @ForeignKey(
                entity = RaceEntity.class,
                parentColumns = "key",
                childColumns = "raceKey",
                onDelete = ForeignKey.CASCADE
        )
)
public class TraitEntity {
    @PrimaryKey (autoGenerate = true) public long id;
    @NonNull public String raceKey;

    public String name;
    public String desc;
}
