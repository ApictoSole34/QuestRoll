package com.fizzycoyote.qusetroll.core.models.open5e.language;

import androidx.annotation.Nullable;

import com.fizzycoyote.qusetroll.core.models.open5e.document.DocumentDto;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class LanguageDto implements Serializable {
    @SerializedName("url") public String url;
    @SerializedName("key") public String key;
    @SerializedName("document") public DocumentDto document;
    @SerializedName("name") public String name;
    @SerializedName("desc") public String desc;
    @SerializedName("is_exotic") public boolean isExotic;
    @SerializedName("is_secret") public boolean isSecret;
    @SerializedName("script_language") @Nullable public String scriptLanguage;
}
