package com.murkfeatherstudio.questroll.core.models.custom.custom_weapon_property;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "custom_weapon_properties")
public class CustomWeaponPropertyEntity {
    @PrimaryKey(autoGenerate = true)
    public long id;

    public String key;
    public String name;
    public String desc;
    public String type;
}
