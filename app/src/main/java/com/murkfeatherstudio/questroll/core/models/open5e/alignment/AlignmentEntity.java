package com.murkfeatherstudio.questroll.core.models.open5e.alignment;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "alignments")
public class AlignmentEntity {
    @PrimaryKey
    @NonNull
    public String key;
    public String morality;
    public String societalAttitude;
    @ColumnInfo(name = "short_name") public String shortName;
    public String description;
    @ColumnInfo(name = "document_name") public String documentName;
    @ColumnInfo(name = "document_key") public String documentKey;
}