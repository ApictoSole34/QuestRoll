package com.murkfeatherstudio.questroll.core.models.open5e.species;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "species", indices = {
        @Index("name"),
        @Index("is_subspecies"),
        @Index("document_name")
})
public class SpeciesEntity {

    @PrimaryKey
    @NonNull
    public String key;

    public String name;
    public String desc;

    @ColumnInfo(name = "is_subspecies") public boolean isSubspecies;
    @ColumnInfo(name = "subspecies_of") public String subspeciesOf;

    @ColumnInfo(name = "traits_json") public String traitsJson;

    @ColumnInfo(name = "document_name") public String documentName;
    @ColumnInfo(name = "document_key") public String documentKey;
}
