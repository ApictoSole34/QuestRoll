package com.fizzycoyote.qusetroll.core.local_database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.fizzycoyote.qusetroll.core.models.conventers.CustomConverters;
import com.fizzycoyote.qusetroll.core.models.custom.custom_ability.CustomAbilityDao;
import com.fizzycoyote.qusetroll.core.models.custom.custom_ability.CustomAbilityEntity;
import com.fizzycoyote.qusetroll.core.models.custom.custom_ability.CustomSkillDao;
import com.fizzycoyote.qusetroll.core.models.custom.custom_ability.CustomSkillEntity;
import com.fizzycoyote.qusetroll.core.models.custom.custom_alignment.CustomAlignmentDao;
import com.fizzycoyote.qusetroll.core.models.custom.custom_alignment.CustomAlignmentEntity;
import com.fizzycoyote.qusetroll.core.models.custom.custom_background.CustomBackgroundDao;
import com.fizzycoyote.qusetroll.core.models.custom.custom_background.CustomBackgroundEntity;
import com.fizzycoyote.qusetroll.core.models.custom.custom_character_class.CustomCharacterClassDao;
import com.fizzycoyote.qusetroll.core.models.custom.custom_character_class.CustomCharacterClassEntity;
import com.fizzycoyote.qusetroll.core.models.custom.custom_character_class.custom_class_progression.CustomClassProgressionDao;
import com.fizzycoyote.qusetroll.core.models.custom.custom_character_class.custom_class_progression.CustomClassProgressionEntity;
import com.fizzycoyote.qusetroll.core.models.custom.custom_character_class.custom_feature.CustomFeatureDao;
import com.fizzycoyote.qusetroll.core.models.custom.custom_character_class.custom_feature.CustomFeatureEntity;
import com.fizzycoyote.qusetroll.core.models.custom.custom_condition.CustomConditionDao;
import com.fizzycoyote.qusetroll.core.models.custom.custom_condition.CustomConditionEntity;
import com.fizzycoyote.qusetroll.core.models.custom.custom_creature.CustomCreatureDao;
import com.fizzycoyote.qusetroll.core.models.custom.custom_creature.CustomCreatureEntity;
import com.fizzycoyote.qusetroll.core.models.custom.custom_creature_type.CustomCreatureTypeDao;
import com.fizzycoyote.qusetroll.core.models.custom.custom_creature_type.CustomCreatureTypeEntity;
import com.fizzycoyote.qusetroll.core.models.custom.custom_damage_types.CustomDamageTypeDao;
import com.fizzycoyote.qusetroll.core.models.custom.custom_damage_types.CustomDamageTypeEntity;
import com.fizzycoyote.qusetroll.core.models.custom.custom_environment.CustomEnvironmentDao;
import com.fizzycoyote.qusetroll.core.models.custom.custom_environment.CustomEnvironmentEntity;
import com.fizzycoyote.qusetroll.core.models.custom.custom_item.CustomItemDao;
import com.fizzycoyote.qusetroll.core.models.custom.custom_item.CustomItemEntity;
import com.fizzycoyote.qusetroll.core.models.custom.custom_item_category.CustomItemCategoryDao;
import com.fizzycoyote.qusetroll.core.models.custom.custom_item_category.CustomItemCategoryEntity;
import com.fizzycoyote.qusetroll.core.models.custom.custom_item_rarity.CustomItemRarityDao;
import com.fizzycoyote.qusetroll.core.models.custom.custom_item_rarity.CustomItemRarityEntity;
import com.fizzycoyote.qusetroll.core.models.custom.custom_item_set.CustomItemSetDao;
import com.fizzycoyote.qusetroll.core.models.custom.custom_item_set.CustomItemSetEntity;
import com.fizzycoyote.qusetroll.core.models.custom.custom_language.CustomLanguageDao;
import com.fizzycoyote.qusetroll.core.models.custom.custom_language.CustomLanguageEntity;
import com.fizzycoyote.qusetroll.core.models.custom.custom_service.CustomServiceDao;
import com.fizzycoyote.qusetroll.core.models.custom.custom_service.CustomServiceEntity;
import com.fizzycoyote.qusetroll.core.models.custom.custom_species.CustomSpeciesDao;
import com.fizzycoyote.qusetroll.core.models.custom.custom_species.CustomSpeciesEntity;
import com.fizzycoyote.qusetroll.core.models.custom.custom_spell.CustomSpellDao;
import com.fizzycoyote.qusetroll.core.models.custom.custom_spell.CustomSpellEntity;
import com.fizzycoyote.qusetroll.core.models.custom.custom_spell.CustomSpellSchoolDao;
import com.fizzycoyote.qusetroll.core.models.custom.custom_spell.CustomSpellSchoolEntity;
import com.fizzycoyote.qusetroll.core.models.custom.custom_weapon_property.CustomWeaponPropertyDao;
import com.fizzycoyote.qusetroll.core.models.custom.custom_weapon_property.CustomWeaponPropertyEntity;

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
        CustomItemEntity.class,
        CustomDamageTypeEntity.class,
        CustomAbilityEntity.class,
        CustomSkillEntity.class,
        CustomAlignmentEntity.class,
        CustomItemRarityEntity.class,
        CustomWeaponPropertyEntity.class,
        CustomServiceEntity.class,
        CustomEnvironmentEntity.class,
        CustomConditionEntity.class,
        CustomItemCategoryEntity.class,
        CustomItemSetEntity.class,
        },
        version = 41,
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
    public abstract CustomItemDao customItemDao();
    public abstract CustomDamageTypeDao customDamageTypeDao();
    public abstract CustomAbilityDao customAbilityDao();
    public abstract CustomSkillDao customSkillDao();
    public abstract CustomAlignmentDao customAlignmentDao();
    public abstract CustomItemRarityDao customItemRarityDao();
    public abstract CustomWeaponPropertyDao customWeaponPropertyDao();
    public abstract CustomServiceDao customServiceDao();
    public abstract CustomEnvironmentDao customEnvironmentDao();
    public abstract CustomConditionDao customConditionDao();
    public abstract CustomItemCategoryDao customItemCategoryDao();
    public abstract CustomItemSetDao customItemSetDao();

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
