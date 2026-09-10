package com.murkfeatherstudio.questroll.core.models.custom.custom_spell;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.murkfeatherstudio.questroll.core.models.conventers.CustomConverters;

import java.util.ArrayList;
import java.util.List;

@Entity(tableName = "custom_spells")
public class CustomSpellEntity implements Parcelable {

    @PrimaryKey(autoGenerate = true)
    public long id;

    @NonNull
    public String name = "";
    public String desc = "";
    public int level = 0;

    @ColumnInfo(name = "school_name")
    public String schoolName = "";

    @ColumnInfo(name = "casting_time")
    public String castingTime = "";

    @ColumnInfo(name = "range_text")
    public String rangeText = "";

    public String duration = "";

    public boolean verbal = false;
    public boolean somatic = false;
    public boolean material = false;

    @ColumnInfo(name = "material_specified")
    public String materialSpecified = "";

    public boolean ritual = false;
    public boolean concentration = false;

    @ColumnInfo(name = "saving_throw_ability")
    public String savingThrowAbility = "";

    @ColumnInfo(name = "attack_roll")
    public boolean attackRoll = false;

    @ColumnInfo(name = "damage_roll")
    public String damageRoll = "";

    @TypeConverters(CustomConverters.class)
    @ColumnInfo(name = "damage_types")
    public List<String> damageTypes = new ArrayList<>();

    @ColumnInfo(name = "higher_level")
    public String higherLevel = "";

    @ColumnInfo(name = "casting_options_json")
    public String castingOptionsJson = "";

    @ColumnInfo(name = "game_system")
    public String gameSystem = "5e-2014"; // Default to 2014

    public CustomSpellEntity() {}

    protected CustomSpellEntity(Parcel in) {
        id = in.readLong();
        name = in.readString();
        desc = in.readString();
        level = in.readInt();
        schoolName = in.readString();
        castingTime = in.readString();
        rangeText = in.readString();
        duration = in.readString();
        verbal = in.readByte() != 0;
        somatic = in.readByte() != 0;
        material = in.readByte() != 0;
        materialSpecified = in.readString();
        ritual = in.readByte() != 0;
        concentration = in.readByte() != 0;
        savingThrowAbility = in.readString();
        attackRoll = in.readByte() != 0;
        damageRoll = in.readString();
        damageTypes = in.createStringArrayList();
        higherLevel = in.readString();
        castingOptionsJson = in.readString();
        gameSystem = in.readString();
    }

    public static final Creator<CustomSpellEntity> CREATOR = new Creator<CustomSpellEntity>() {
        @Override
        public CustomSpellEntity createFromParcel(Parcel in) {
            return new CustomSpellEntity(in);
        }
        @Override
        public CustomSpellEntity[] newArray(int size) {
            return new CustomSpellEntity[size];
        }
    };

    @Override
    public int describeContents() { return 0; }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(name);
        dest.writeString(desc);
        dest.writeInt(level);
        dest.writeString(schoolName);
        dest.writeString(castingTime);
        dest.writeString(rangeText);
        dest.writeString(duration);
        dest.writeByte((byte) (verbal ? 1 : 0));
        dest.writeByte((byte) (somatic ? 1 : 0));
        dest.writeByte((byte) (material ? 1 : 0));
        dest.writeString(materialSpecified);
        dest.writeByte((byte) (ritual ? 1 : 0));
        dest.writeByte((byte) (concentration ? 1 : 0));
        dest.writeString(savingThrowAbility);
        dest.writeByte((byte) (attackRoll ? 1 : 0));
        dest.writeString(damageRoll);
        dest.writeStringList(damageTypes);
        dest.writeString(higherLevel);
        dest.writeString(castingOptionsJson);
        dest.writeString(gameSystem);
    }
}
