package com.fizzycoyote.qusetroll.core.local_database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.fizzycoyote.qusetroll.core.models.conventers.CustomConverters;
import com.fizzycoyote.qusetroll.core.models.custom.custom_background.CustomBackgroundDao;
import com.fizzycoyote.qusetroll.core.models.custom.custom_background.CustomBackgroundEntity;
import com.fizzycoyote.qusetroll.core.models.custom.custom_character_class.CustomCharacterClassDao;
import com.fizzycoyote.qusetroll.core.models.custom.custom_character_class.CustomCharacterClassEntity;
import com.fizzycoyote.qusetroll.core.models.custom.custom_character_class.custom_class_progression.CustomClassProgressionDao;
import com.fizzycoyote.qusetroll.core.models.custom.custom_character_class.custom_class_progression.CustomClassProgressionEntity;
import com.fizzycoyote.qusetroll.core.models.custom.custom_character_class.custom_feature.CustomFeatureDao;
import com.fizzycoyote.qusetroll.core.models.custom.custom_character_class.custom_feature.CustomFeatureEntity;
import com.fizzycoyote.qusetroll.core.models.custom.custom_creature.CustomCreatureDao;
import com.fizzycoyote.qusetroll.core.models.custom.custom_creature.CustomCreatureEntity;
import com.fizzycoyote.qusetroll.core.models.custom.custom_creature.CustomCreatureTypeDao;
import com.fizzycoyote.qusetroll.core.models.custom.custom_creature.CustomCreatureTypeEntity;
import com.fizzycoyote.qusetroll.core.models.custom.custom_language.CustomLanguageDao;
import com.fizzycoyote.qusetroll.core.models.custom.custom_language.CustomLanguageEntity;
import com.fizzycoyote.qusetroll.core.models.custom.custom_species.CustomSpeciesDao;
import com.fizzycoyote.qusetroll.core.models.custom.custom_species.CustomSpeciesEntity;
import com.fizzycoyote.qusetroll.core.models.custom.custom_spell.CustomSpellDao;
import com.fizzycoyote.qusetroll.core.models.custom.custom_spell.CustomSpellEntity;
import com.fizzycoyote.qusetroll.core.models.custom.custom_spell.CustomSpellSchoolDao;
import com.fizzycoyote.qusetroll.core.models.custom.custom_spell.CustomSpellSchoolEntity;
import com.fizzycoyote.qusetroll.core.models.custom.custom_weapon.CustomWeaponEntity;


@Database(
        entities = {CustomLanguageEntity.class,
        CustomCharacterClassEntity.class,
        CustomFeatureEntity.class,
        CustomClassProgressionEntity.class,
        CustomSpellEntity.class,
        CustomSpellSchoolEntity.class,
        CustomCreatureEntity.class,
        CustomCreatureTypeEntity.class,
        CustomSpeciesEntity.class,
        CustomBackgroundEntity.class,
        CustomWeaponEntity.class},
        version = 16,
        exportSchema = false
)
@TypeConverters({CustomConverters.class})
public abstract class UserContentDatabase extends RoomDatabase {
    private static volatile UserContentDatabase INSTANCE;

    public abstract CustomLanguageDao customLanguageDao();
    public abstract CustomCharacterClassDao customCharacterClassDao();
    public abstract CustomFeatureDao customFeatureDao();
    public abstract CustomClassProgressionDao customClassProgressionDao();
    public abstract CustomSpellDao customSpellDao();
    public abstract CustomSpellSchoolDao customSpellSchoolDao();
    public abstract CustomCreatureDao customCreatureDao();
    public abstract CustomCreatureTypeDao customCreatureTypeDao();
    public abstract CustomSpeciesDao customSpeciesDao();
    public abstract CustomBackgroundDao customBackgroundDao();
    public abstract CustomWeaponDao customWeaponDao();





    public static UserContentDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (UserContentDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            UserContentDatabase.class,
                            "user_content_database"
                    ).fallbackToDestructiveMigration().build();
                }
            }
        }
        return INSTANCE;
    }
}
