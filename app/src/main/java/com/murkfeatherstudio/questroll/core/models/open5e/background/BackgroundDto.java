package com.murkfeatherstudio.questroll.core.models.open5e.background;

import com.murkfeatherstudio.questroll.core.models.open5e.document.DocumentDto;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class BackgroundDto implements Serializable {
    @SerializedName("key") public String key;
    @SerializedName("name") public String name;
    @SerializedName("desc") public String desc;
    @SerializedName("document") public DocumentDto document;
    @SerializedName("benefits") public List<BenefitDto> benefits;

    public static class BenefitDto implements Serializable {
        @SerializedName("name") public String name;
        @SerializedName("desc") public String desc;
        @SerializedName("type") public String type;
    }
}