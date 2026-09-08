package com.murkfeatherstudio.questroll.core.models.custom.custom_language;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "custom_languages")
public class CustomLanguageEntity {
    @PrimaryKey(autoGenerate = true) public long id;
    @NonNull @ColumnInfo(name = "name") public String name;
    @ColumnInfo(name = "description") public String desc;
    @ColumnInfo(name = "is_exotic", defaultValue = "false") public boolean isExotic;
    @ColumnInfo(name = "is_secret", defaultValue = "false") public boolean isSecret;
    @Nullable @ColumnInfo(name = "script_language") public String scriptLanguageId;
    @Ignore public String DocumentUrl;


    public CustomLanguageEntity(@NonNull String name, String desc) {
        this.name = name;
        this.desc = desc;
    }
}
