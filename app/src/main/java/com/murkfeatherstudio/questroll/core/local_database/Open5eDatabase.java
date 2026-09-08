package com.murkfeatherstudio.questroll.core.local_database;


import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.murkfeatherstudio.questroll.core.models.open5e.Converters;
import com.murkfeatherstudio.questroll.core.models.open5e.ability.AbilityDao;
import com.murkfeatherstudio.questroll.core.models.open5e.ability.AbilityEntity;
import com.murkfeatherstudio.questroll.core.models.open5e.ability.skill.SkillDao;
import com.murkfeatherstudio.questroll.core.models.open5e.ability.skill.SkillEntity;
import com.murkfeatherstudio.questroll.core.models.open5e.alignment.AlignmentDao;
import com.murkfeatherstudio.questroll.core.models.open5e.alignment.AlignmentEntity;
import com.murkfeatherstudio.questroll.core.models.open5e.background.BackgroundDao;
import com.murkfeatherstudio.questroll.core.models.open5e.background.BackgroundEntity;
import com.murkfeatherstudio.questroll.core.models.open5e.character_class.CharacterClassDao;
import com.murkfeatherstudio.questroll.core.models.open5e.character_class.CharacterClassEntity;
import com.murkfeatherstudio.questroll.core.models.open5e.character_class.feature.FeatureDao;
import com.murkfeatherstudio.questroll.core.models.open5e.character_class.feature.FeatureEntity;
import com.murkfeatherstudio.questroll.core.models.open5e.character_class.gained_at.GainedAtListConverter;
import com.murkfeatherstudio.questroll.core.models.open5e.character_class.hit_points.HitPointsDao;
import com.murkfeatherstudio.questroll.core.models.open5e.character_class.hit_points.HitPointsEntity;
import com.murkfeatherstudio.questroll.core.models.open5e.character_class.saving_throw.SavingThrowDao;
import com.murkfeatherstudio.questroll.core.models.open5e.character_class.saving_throw.SavingThrowEntity;
import com.murkfeatherstudio.questroll.core.models.open5e.character_class.table_data.TableDataListConverter;
import com.murkfeatherstudio.questroll.core.models.open5e.condition.ConditionDao;
import com.murkfeatherstudio.questroll.core.models.open5e.condition.ConditionEntity;
import com.murkfeatherstudio.questroll.core.models.open5e.creature.CreatureDao;
import com.murkfeatherstudio.questroll.core.models.open5e.creature.CreatureEntity;
import com.murkfeatherstudio.questroll.core.models.open5e.creature_type.CreatureTypeDao;
import com.murkfeatherstudio.questroll.core.models.open5e.creature_type.CreatureTypeEntity;
import com.murkfeatherstudio.questroll.core.models.open5e.damage_type.DamageTypeDao;
import com.murkfeatherstudio.questroll.core.models.open5e.damage_type.DamageTypeEntity;
import com.murkfeatherstudio.questroll.core.models.open5e.document.DocumentDao;
import com.murkfeatherstudio.questroll.core.models.open5e.document.DocumentEntity;
import com.murkfeatherstudio.questroll.core.models.open5e.environment.EnvironmentDao;
import com.murkfeatherstudio.questroll.core.models.open5e.environment.EnvironmentEntity;
import com.murkfeatherstudio.questroll.core.models.open5e.game_system.GameSystemDao;
import com.murkfeatherstudio.questroll.core.models.open5e.game_system.GameSystemEntity;
import com.murkfeatherstudio.questroll.core.models.open5e.item.ItemDao;
import com.murkfeatherstudio.questroll.core.models.open5e.item.ItemEntity;
import com.murkfeatherstudio.questroll.core.models.open5e.item_category.ItemCategoryDao;
import com.murkfeatherstudio.questroll.core.models.open5e.item_category.ItemCategoryEntity;
import com.murkfeatherstudio.questroll.core.models.open5e.item_rarity.ItemRarityDao;
import com.murkfeatherstudio.questroll.core.models.open5e.item_rarity.ItemRarityEntity;
import com.murkfeatherstudio.questroll.core.models.open5e.item_set.ItemSetDao;
import com.murkfeatherstudio.questroll.core.models.open5e.item_set.ItemSetEntity;
import com.murkfeatherstudio.questroll.core.models.open5e.language.LanguageDao;
import com.murkfeatherstudio.questroll.core.models.open5e.language.LanguageEntity;
import com.murkfeatherstudio.questroll.core.models.open5e.license.LicenseDao;
import com.murkfeatherstudio.questroll.core.models.open5e.license.LicenseEntity;
import com.murkfeatherstudio.questroll.core.models.open5e.publisher.PublisherDao;
import com.murkfeatherstudio.questroll.core.models.open5e.publisher.PublisherEntity;
import com.murkfeatherstudio.questroll.core.models.open5e.rule.RuleDao;
import com.murkfeatherstudio.questroll.core.models.open5e.rule.RuleEntity;
import com.murkfeatherstudio.questroll.core.models.open5e.rule_set.RulesetDao;
import com.murkfeatherstudio.questroll.core.models.open5e.rule_set.RulesetEntity;
import com.murkfeatherstudio.questroll.core.models.open5e.service.ServiceDao;
import com.murkfeatherstudio.questroll.core.models.open5e.service.ServiceEntity;
import com.murkfeatherstudio.questroll.core.models.open5e.species.SpeciesDao;
import com.murkfeatherstudio.questroll.core.models.open5e.species.SpeciesEntity;
import com.murkfeatherstudio.questroll.core.models.open5e.spell.SpellDao;
import com.murkfeatherstudio.questroll.core.models.open5e.spell.SpellEntity;
import com.murkfeatherstudio.questroll.core.models.open5e.spell_school.SpellSchoolDao;
import com.murkfeatherstudio.questroll.core.models.open5e.spell_school.SpellSchoolEntity;
import com.murkfeatherstudio.questroll.core.models.open5e.weapon_property.WeaponPropertyDao;
import com.murkfeatherstudio.questroll.core.models.open5e.weapon_property.WeaponPropertyEntity;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * The main Room database for storing D&D 5e game content fetched from the Open5e API.
 * <p>
 * This database acts as a local cache for all SRD and Open5e game data, providing
 * offline access to classes, spells, items, monsters, and rules.
 * </p>
 */
@Database(
        entities = {
                LicenseEntity.class,
                PublisherEntity.class,
                GameSystemEntity.class,
                DocumentEntity.class,
                LanguageEntity.class,
                AbilityEntity.class,
                SkillEntity.class,
                CharacterClassEntity.class,
                FeatureEntity.class,
                HitPointsEntity.class,
                SavingThrowEntity.class,
                SpellEntity.class,
                SpellSchoolEntity.class,
                CreatureEntity.class,
                SpeciesEntity.class,
                BackgroundEntity.class,
                ItemEntity.class,
                DamageTypeEntity.class,
                AlignmentEntity.class,
                ItemRarityEntity.class,
                WeaponPropertyEntity.class,
                ServiceEntity.class,
                EnvironmentEntity.class,
                RuleEntity.class,
                RulesetEntity.class,
                ConditionEntity.class,
                CreatureTypeEntity.class,
                ItemCategoryEntity.class,
                ItemSetEntity.class,
        },
        version = 49,
        exportSchema = false
)
@TypeConverters({GainedAtListConverter.class, TableDataListConverter.class, Converters.class})
public abstract class Open5eDatabase extends RoomDatabase {
    private static volatile Open5eDatabase INSTANCE;

    public abstract DocumentDao documentDao();
    public abstract GameSystemDao gameSystemDao();
    public abstract LicenseDao licenseDao();
    public abstract LanguageDao languageDao();
    public abstract PublisherDao publisherDao();
    public abstract AbilityDao abilityDao();
    public abstract SkillDao skillDao();
    public abstract CharacterClassDao characterClassDao();
    public abstract FeatureDao featureDao();
    public abstract HitPointsDao hitPointsDao();
    public abstract SavingThrowDao savingThrowDao();
    public abstract SpellDao spellDao();
    public abstract SpellSchoolDao spellSchoolDao();
    public abstract CreatureDao creatureDao();
    public abstract SpeciesDao speciesDao();
    public abstract BackgroundDao backgroundDao();
    public abstract ItemDao itemDao();
    public abstract DamageTypeDao damageTypeDao();
    public abstract AlignmentDao alignmentDao();
    public abstract ItemRarityDao itemRarityDao();
    public abstract WeaponPropertyDao weaponPropertyDao();
    public abstract ServiceDao serviceDao();
    public abstract EnvironmentDao environmentDao();
    public abstract RuleDao ruleDao();
    public abstract RulesetDao rulesetDao();
    public abstract ConditionDao conditionDao();
    public abstract CreatureTypeDao creatureTypeDao();
    public abstract ItemCategoryDao itemCategoryDao();
    public abstract ItemSetDao itemSetDao();

    /**
     * Gets the singleton instance of the Open5eDatabase.
     *
     * @param context The application context.
     * @return The singleton instance.
     */
    public static Open5eDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (Open5eDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    Open5eDatabase.class,
                                    "open5e_database"
                            )
                            .addCallback(new Callback() {
                                @Override
                                public void onCreate(@NonNull SupportSQLiteDatabase db) {
                                    super.onCreate(db);
                                    Log.d("AppDatabase", "onCreate: open5e_database CREATED");
                                }

                                @Override
                                public void onOpen(@NonNull SupportSQLiteDatabase db) {
                                    super.onOpen(db);
                                    Log.d("AppDatabase", "onOpen: open5e_database OPENED");
                                }
                            })
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    private static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(4);

    /**
     * Returns an executor for running database queries in the background.
     *
     * @return An {@link Executor} with a fixed thread pool.
     */
    public Executor getQueryExecutor() {
        return databaseWriteExecutor;
    }
}
