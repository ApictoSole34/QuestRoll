package com.murkfeatherstudio.questroll.feature_alignment.model;

import com.murkfeatherstudio.questroll.core.models.custom.custom_alignment.CustomAlignmentEntity;
import com.murkfeatherstudio.questroll.core.models.open5e.alignment.AlignmentEntity;

import java.util.Objects;

public class CombinedAlignment {
    public final String id;
    public final String name;
    public final String shortName;
    public final String morality;
    public final String societalAttitude;
    public final String description;
    public final boolean isCustom;
    public final String key;
    public final long customId;

    public CombinedAlignment(AlignmentEntity a) {
        this.id = a.key;
        this.key = a.key;
        this.customId = -1;
        this.name = formatAlignmentKey(a.key);
        this.shortName = a.shortName;
        this.morality = a.morality;
        this.societalAttitude = a.societalAttitude;
        this.description = a.description;
        this.isCustom = false;
    }

    public CombinedAlignment(CustomAlignmentEntity a) {
        this.id = "custom_" + a.id;
        this.key = null;
        this.customId = a.id;
        this.name = a.name;
        this.shortName = a.shortName;
        this.morality = a.morality;
        this.societalAttitude = a.societalAttitude;
        this.description = a.description;
        this.isCustom = true;
    }

    public static String formatAlignmentKey(String key) {
        if (key == null) return "";
        String[] words = key.split("-");
        StringBuilder sb = new StringBuilder();
        for (String word : words) {
            if (sb.length() > 0) sb.append(" ");
            sb.append(Character.toUpperCase(word.charAt(0)) + word.substring(1));
        }
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return Objects.equals(id, ((CombinedAlignment) o).id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}