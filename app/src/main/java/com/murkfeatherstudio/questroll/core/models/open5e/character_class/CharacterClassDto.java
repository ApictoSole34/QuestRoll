package com.murkfeatherstudio.questroll.core.models.open5e.character_class;

import com.murkfeatherstudio.questroll.core.models.open5e.character_class.feature.FeatureDto;
import com.murkfeatherstudio.questroll.core.models.open5e.character_class.hit_points.HitPointsDto;
import com.murkfeatherstudio.questroll.core.models.open5e.character_class.saving_throw.SavingTrowDto;
import com.murkfeatherstudio.questroll.core.models.open5e.character_class.subclass_of.SubclassOfDto;
import com.murkfeatherstudio.questroll.core.models.open5e.document.DocumentDto;
import com.google.gson.annotations.SerializedName;

import org.jspecify.annotations.Nullable;

import java.util.List;

public class CharacterClassDto {
    @SerializedName("key") public String key;
    @SerializedName("name") public String name;
    @SerializedName("document") public DocumentDto document;
    @SerializedName("saving_throws") public List<SavingTrowDto> savingThrows;
    @SerializedName("caster_type") @Nullable public String casterType;
    @SerializedName("subclass_of") @Nullable public SubclassOfDto subclassOf;
    @SerializedName("hit_points") @Nullable public HitPointsDto hitPoints;
    @SerializedName("features") public List<FeatureDto> features;
}
