package com.murkfeatherstudio.questroll.core.models.open5e.character_class.hit_points;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.murkfeatherstudio.questroll.core.models.open5e.character_class.CharacterClassEntity;

@Entity(
        tableName = "hitpoints",
        foreignKeys = @ForeignKey(
                entity = CharacterClassEntity.class,
                parentColumns = "class_key",
                childColumns = "class_key_ref",
                onDelete = ForeignKey.CASCADE
        ),
        indices = @Index("class_key_ref")
)
public class HitPointsEntity {
    @PrimaryKey(autoGenerate = true)
    public int id;

    @NonNull
    @ColumnInfo(name = "class_key_ref")
    public String classKey;

    public String hitDice;
    public String hitDiceName;
    public String at1stLevel;
    public String atHigherLevels;
}