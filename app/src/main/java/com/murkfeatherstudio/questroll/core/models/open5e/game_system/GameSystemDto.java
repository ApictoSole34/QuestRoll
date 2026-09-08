package com.murkfeatherstudio.questroll.core.models.open5e.game_system;

import androidx.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class GameSystemDto implements Serializable {
    @SerializedName("url") public String url;
    @SerializedName("key") public String key;
    @SerializedName("name") public String name;
    @SerializedName("desc") public String desc;
    @SerializedName("content_prefix") @Nullable public String contentPrefix;
}
