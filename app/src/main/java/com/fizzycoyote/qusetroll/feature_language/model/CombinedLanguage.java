package com.fizzycoyote.qusetroll.feature_language.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Objects;

/**
 *Unified model representing a language from either Open5e or custom sources.
 *Implements Parcelable for Android IPC.
 */
public class CombinedLanguage implements Parcelable {
    public final String name;
    public final String desc;
    public final boolean exotic;
    public final boolean secret;
    public String scriptLanguageName; //display
    public final String scriptOpenUrl;   // non-null if its from open5e
    public final String scriptCustomKey; // non-null if its from custom
    public final String documentUrl;
    public final String open5eKey;
    public final Long customId;

    public CombinedLanguage(String name, String desc, boolean exotic, boolean secret,
                            String scriptLanguageName, String scriptOpenUrl, String scriptCustomKey, String documentUrl,
                            String open5eKey, Long customId) {
        this.name = name;
        this.desc = desc;
        this.exotic = exotic;
        this.secret = secret;
        this.scriptLanguageName = scriptLanguageName;
        this.scriptOpenUrl = scriptOpenUrl;
        this.scriptCustomKey = scriptCustomKey;
        this.documentUrl = documentUrl;
        this.open5eKey = open5eKey;
        this.customId = customId;
    }

    protected CombinedLanguage(Parcel in) {
        name = in.readString();
        desc = in.readString();
        exotic = in.readByte() != 0;
        secret = in.readByte() != 0;
        scriptLanguageName = in.readString();
        scriptOpenUrl        = in.readString();
        scriptCustomKey      = in.readString();
        documentUrl = in.readString();
        open5eKey = in.readString();
        if (in.readByte() == 0) {
            customId = null;
        } else {
            customId = in.readLong();
        }
    }

    public static final Creator<CombinedLanguage> CREATOR = new Creator<CombinedLanguage>() {
        @Override public CombinedLanguage createFromParcel(Parcel in) { return new CombinedLanguage(in); }
        @Override public CombinedLanguage[] newArray(int size) { return new CombinedLanguage[size]; }
    };

    @Override public int describeContents() { return 0; }

    @Override public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(desc);
        dest.writeByte((byte) (exotic ? 1 : 0));
        dest.writeByte((byte) (secret ? 1 : 0));
        dest.writeString(scriptLanguageName);
        dest.writeString(scriptOpenUrl);
        dest.writeString(scriptCustomKey);
        dest.writeString(documentUrl);
        dest.writeString(open5eKey);
        if (customId == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeLong(customId);
        }
    }

    /**
     *Extracts the last segment of Open5e URLs (e.g., "/api/languages/elvish" → "elvish")
     *Returns customKey directly for custom scripts
     */
    public String getScriptKey() {
        if (scriptOpenUrl != null) {
            String[] parts = scriptOpenUrl.split("/");
            return parts.length > 0 ? parts[parts.length - 1] : scriptOpenUrl;
        }
        return scriptCustomKey;
    }

    public String getUniqueKey() {
        return isCustom() ? "custom_" + customId : "open5e_" + open5eKey;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CombinedLanguage that = (CombinedLanguage) o;
        return exotic == that.exotic &&
                secret == that.secret &&
                Objects.equals(name, that.name) &&
                Objects.equals(desc, that.desc) &&
                Objects.equals(scriptLanguageName, that.scriptLanguageName) &&
                Objects.equals(customId, that.customId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, desc, exotic, secret, scriptLanguageName, customId);
    }

    public String getName() {
        return name;
    }

    public String getDesc() {
        return desc;
    }

    public boolean isExotic() {
        return exotic;
    }

    public boolean isSecret() {
        return secret;
    }

    public boolean isCustom() {
        return customId != null;
    }

    public String getScriptLanguageName() {
        return scriptLanguageName;
    }

    public String getDocumentUrl() {
        return documentUrl;
    }

    public String getOpen5eKey() {
        return open5eKey;
    }

    public Long getCustomId() {
        return customId;
    }
}
