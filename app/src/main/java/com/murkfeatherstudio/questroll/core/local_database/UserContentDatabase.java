package com.murkfeatherstudio.questroll.core.local_database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.murkfeatherstudio.questroll.core.models.conventers.CustomConverters;
import com.murkfeatherstudio.questroll.core.models.custom.custom_ability.CustomAbilityDao;
import com.murkfeatherstudio.questroll.core.models.custom.custom_ability.CustomAbilityEntity;
import com.murkfeatherstudio.questroll.core.models.custom.custom_ability.CustomSkillDao;
import com.murkfeatherstudio.questroll.core.models.custom.custom_ability.CustomSkillEntity;
import com.murkfeatherstudio.questroll.core.models.custom.custom_alignment.CustomAlignmentDao;
import com.murkfeatherstudio.questroll.core.models.custom.custom_alignment.CustomAlignmentEntity;
import com.murkfeatherstudio.questroll.core.models.custom.custom_background.CustomBackgroundDao;
import com.murkfeatherstudio.questroll.core.models.custom.custom_background.CustomBackgroundEntity;
import com.murkfeatherstudio.questroll.core.models.custom.custom_character_class.CustomCharacterClassDao;
import com.murkfeatherstudio.questroll.core.models.custom.custom_character_class.CustomCharacterClassEntity;
import com.murkfeatherstudio.questroll.core.models.custom.custom_character_class.custom_class_progression.CustomClassProgressionDao;
import com.murkfeatherstudio.questroll.core.models.custom.custom_character_class.custom_class_progression.CustomClassProgressionEntity;
import com.murkfeatherstudio.questroll.core.models.custom.custom_character_class.custom_feature.CustomFeatureDao;
import com.murkfeatherstudio.questroll.core.models.custom.custom_character_class.custom_feature.CustomFeatureEntity;
import com.murkfeatherstudio.questroll.core.models.custom.custom_condition.CustomConditionDao;
import com.murkfeatherstudio.questroll.core.models.custom.custom_condition.CustomConditionEntity;
import com.murkfeatherstudio.questroll.core.models.custom.custom_creature.CustomCreatureDao;
import com.murkfeatherstudio.questroll.core.models.custom.custom_creature.CustomCreatureEntity;
import com.murkfeatherstudio.questroll.core.models.custom.custom_creature_type.CustomCreatureTypeDao;
import com.murkfeatherstudio.questroll.core.models.custom.custom_creature_type.CustomCreatureTypeEntity;
import com.murkfeatherstudio.questroll.core.models.custom.custom_damage_types.CustomDamageTypeDao;
import com.murkfeatherstudio.questroll.core.models.custom.custom_damage_types.CustomDamageTypeEntity;
import com.murkfeatherstudio.questroll.core.models.custom.custom_environment.CustomEnvironmentDao;
import com.murkfeatherstudio.questroll.core.models.custom.custom_environment.CustomEnvironmentEntity;
import com.murkfeatherstudio.questroll.core.models.custom.custom_item.CustomItemDao;
import com.murkfeatherstudio.questroll.core.models.custom.custom_item.CustomItemEntity;
import com.murkfeatherstudio.questroll.core.models.custom.custom_item_category.CustomItemCategoryDao;
import com.murkfeatherstudio.questroll.core.models.custom.custom_item_category.CustomItemCategoryEntity;
import com.murkfeatherstudio.questroll.core.models.custom.custom_item_rarity.CustomItemRarityDao;
import com.murkfeatherstudio.questroll.core.models.custom.custom_item_rarity.CustomItemRarityEntity;
import com.murkfeatherstudio.questroll.core.models.custom.custom_item_set.CustomItemSetDao;
import com.murkfeatherstudio.questroll.core.models.custom.custom_item_set.CustomItemSetEntity;
import com.murkfeatherstudio.questroll.core.models.custom.custom_language.CustomLanguageDao;
import com.murkfeatherstudio.questroll.core.models.custom.custom_language.CustomLanguageEntity;
import com.murkfeatherstudio.questroll.core.models.custom.custom_service.CustomServiceDao;
import com.murkfeatherstudio.questroll.core.models.custom.custom_service.CustomServiceEntity;
import com.murkfeatherstudio.questroll.core.models.custom.custom_species.CustomSpeciesDao;
import com.murkfeatherstudio.questroll.core.models.custom.custom_species.CustomSpeciesEntity;
import com.murkfeatherstudio.questroll.core.models.custom.custom_spell.CustomSpellDao;
import com.murkfeatherstudio.questroll.core.models.custom.custom_spell.CustomSpellEntity;
import com.murkfeatherstudio.questroll.core.models.custom.custom_spell.CustomSpellSchoolDao;
import com.murkfeatherstudio.questroll.core.models.custom.custom_spell.CustomSpellSchoolEntity;
import com.murkfeatherstudio.questroll.core.models.custom.custom_weapon_property.CustomWeaponPropertyDao;
import com.murkfeatherstudio.questroll.core.models.custom.custom_weapon_property.CustomWeaponPropertyEntity;
import com.murkfeatherstudio.questroll.core.models.custom.pdf.CustomPdfDao;
import com.murkfeatherstudio.questroll.core.models.custom.pdf.CustomPdfEntity;

/**
 * Room database for storing user-created custom game content.
 * <p>
 * This database allows users to extend the base game with their own homebrew
 * classes, spells, items, monsters, and more. It is separate from the official
 * {@link Open5eDatabase} and the character data in {@link PlayerCharacterDatabase}.
 * </p>
 */
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
        CustomPdfEntity.class
        },
        version = 42,
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
    public abstract CustomPdfDao customPdfDao();

    /**
     * Gets the singleton instance of the UserContentDatabase.
     *
     * @param context The application context.
     * @return The singleton instance.
     */
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
