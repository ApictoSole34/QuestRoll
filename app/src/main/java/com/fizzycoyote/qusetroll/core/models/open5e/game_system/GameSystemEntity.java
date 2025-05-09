package com.fizzycoyote.qusetroll.core.models.open5e.game_system;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "game_systems")
public class GameSystemEntity {
    @PrimaryKey @NonNull public String key;
    public String url;
    public String name;
    public String desc;
    public String contentPrefix;
}
