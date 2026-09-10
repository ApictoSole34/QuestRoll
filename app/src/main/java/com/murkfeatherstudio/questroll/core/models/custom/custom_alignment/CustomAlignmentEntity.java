package com.murkfeatherstudio.questroll.core.models.custom.custom_alignment;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "custom_alignments")
public class CustomAlignmentEntity {
    @PrimaryKey(autoGenerate = true)
    public long id;

    public String key;
    public String name;
    public String shortName;
    public String morality;
    public String societalAttitude;
    public String description;

    @ColumnInfo(name = "game_system")
    public String gameSystem = "5e-2014";
}