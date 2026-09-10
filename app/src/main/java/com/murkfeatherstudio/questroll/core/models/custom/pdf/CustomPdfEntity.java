package com.murkfeatherstudio.questroll.core.models.custom.pdf;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "custom_pdfs")
public class CustomPdfEntity {
    @PrimaryKey(autoGenerate = true)
    public long id;

    public String name;
    public String uri;
    public long dateAdded;

    @ColumnInfo(name = "game_system")
    public String gameSystem = "5e-2014";
}
