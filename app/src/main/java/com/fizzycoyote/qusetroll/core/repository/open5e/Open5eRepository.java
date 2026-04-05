package com.fizzycoyote.qusetroll.core.repository.open5e;

import android.util.Log;

import com.fizzycoyote.qusetroll.core.api.Open5eApiService;
import com.fizzycoyote.qusetroll.core.models.open5e.ability.AbilityDao;
import com.fizzycoyote.qusetroll.core.models.open5e.ability.AbilityDto;
import com.fizzycoyote.qusetroll.core.models.open5e.ability.AbilityEntity;
import com.fizzycoyote.qusetroll.core.models.open5e.ability.AbilityMapper;
import com.fizzycoyote.qusetroll.core.models.open5e.ability.AbilityResponse;
import com.fizzycoyote.qusetroll.core.models.open5e.ability.skill.SkillDao;
import com.fizzycoyote.qusetroll.core.models.open5e.ability.skill.SkillEntity;
import com.fizzycoyote.qusetroll.core.models.open5e.alignment.AlignmentDao;
import com.fizzycoyote.qusetroll.core.models.open5e.alignment.AlignmentEntity;
import com.fizzycoyote.qusetroll.core.models.open5e.alignment.AlignmentMapper;
import com.fizzycoyote.qusetroll.core.models.open5e.alignment.AlignmentResponse;
import com.fizzycoyote.qusetroll.core.models.open5e.background.BackgroundDao;
import com.fizzycoyote.qusetroll.core.models.open5e.background.BackgroundEntity;
import com.fizzycoyote.qusetroll.core.models.open5e.background.BackgroundMapper;
import com.fizzycoyote.qusetroll.core.models.open5e.background.BackgroundResponse;
import com.fizzycoyote.qusetroll.core.models.open5e.character_class.CharacterClassDao;
import com.fizzycoyote.qusetroll.core.models.open5e.character_class.CharacterClassDto;
import com.fizzycoyote.qusetroll.core.models.open5e.character_class.CharacterClassEntity;
import com.fizzycoyote.qusetroll.core.models.open5e.character_class.CharacterClassMapper;
import com.fizzycoyote.qusetroll.core.models.open5e.character_class.CharacterClassResponse;
import com.fizzycoyote.qusetroll.core.models.open5e.character_class.feature.FeatureDao;
import com.fizzycoyote.qusetroll.core.models.open5e.character_class.feature.FeatureEntity;
import com.fizzycoyote.qusetroll.core.models.open5e.character_class.hit_points.HitPointsDao;
import com.fizzycoyote.qusetroll.core.models.open5e.character_class.hit_points.HitPointsEntity;
import com.fizzycoyote.qusetroll.core.models.open5e.character_class.saving_throw.SavingThrowDao;
import com.fizzycoyote.qusetroll.core.models.open5e.character_class.saving_throw.SavingThrowEntity;
import com.fizzycoyote.qusetroll.core.models.open5e.character_class.table_data.TableData;
import com.fizzycoyote.qusetroll.core.models.open5e.condition.ConditionDao;
import com.fizzycoyote.qusetroll.core.models.open5e.condition.ConditionEntity;
import com.fizzycoyote.qusetroll.core.models.open5e.condition.ConditionMapper;
import com.fizzycoyote.qusetroll.core.models.open5e.condition.ConditionResponse;
import com.fizzycoyote.qusetroll.core.models.open5e.creature.CreatureDao;
import com.fizzycoyote.qusetroll.core.models.open5e.creature.CreatureEntity;
import com.fizzycoyote.qusetroll.core.models.open5e.creature.CreatureMapper;
import com.fizzycoyote.qusetroll.core.models.open5e.creature.CreatureResponse;
import com.fizzycoyote.qusetroll.core.models.open5e.creature_type.CreatureTypeDao;
import com.fizzycoyote.qusetroll.core.models.open5e.creature_type.CreatureTypeEntity;
import com.fizzycoyote.qusetroll.core.models.open5e.creature_type.CreatureTypeMapper;
import com.fizzycoyote.qusetroll.core.models.open5e.creature_type.CreatureTypeResponse;
import com.fizzycoyote.qusetroll.core.models.open5e.damage_type.DamageTypeDao;
import com.fizzycoyote.qusetroll.core.models.open5e.damage_type.DamageTypeEntity;
import com.fizzycoyote.qusetroll.core.models.open5e.damage_type.DamageTypeMapper;
import com.fizzycoyote.qusetroll.core.models.open5e.damage_type.DamageTypeResponse;
import com.fizzycoyote.qusetroll.core.models.open5e.document.DocumentDao;
import com.fizzycoyote.qusetroll.core.models.open5e.document.DocumentEntity;
import com.fizzycoyote.qusetroll.core.models.open5e.document.DocumentMapper;
import com.fizzycoyote.qusetroll.core.models.open5e.document.DocumentResponse;
import com.fizzycoyote.qusetroll.core.models.open5e.environment.EnvironmentDao;
import com.fizzycoyote.qusetroll.core.models.open5e.environment.EnvironmentEntity;
import com.fizzycoyote.qusetroll.core.models.open5e.environment.EnvironmentMapper;
import com.fizzycoyote.qusetroll.core.models.open5e.environment.EnvironmentResponse;
import com.fizzycoyote.qusetroll.core.models.open5e.game_system.GameSystemDao;
import com.fizzycoyote.qusetroll.core.models.open5e.game_system.GameSystemEntity;
import com.fizzycoyote.qusetroll.core.models.open5e.game_system.GameSystemMapper;
import com.fizzycoyote.qusetroll.core.models.open5e.game_system.GameSystemResponse;
import com.fizzycoyote.qusetroll.core.models.open5e.item.ItemDao;
import com.fizzycoyote.qusetroll.core.models.open5e.item.ItemEntity;
import com.fizzycoyote.qusetroll.core.models.open5e.item.ItemMapper;
import com.fizzycoyote.qusetroll.core.models.open5e.item.ItemResponse;
import com.fizzycoyote.qusetroll.core.models.open5e.item_category.ItemCategoryDao;
import com.fizzycoyote.qusetroll.core.models.open5e.item_category.ItemCategoryEntity;
import com.fizzycoyote.qusetroll.core.models.open5e.item_category.ItemCategoryMapper;
import com.fizzycoyote.qusetroll.core.models.open5e.item_category.ItemCategoryResponse;
import com.fizzycoyote.qusetroll.core.models.open5e.item_rarity.ItemRarityDao;
import com.fizzycoyote.qusetroll.core.models.open5e.item_rarity.ItemRarityEntity;
import com.fizzycoyote.qusetroll.core.models.open5e.item_rarity.ItemRarityMapper;
import com.fizzycoyote.qusetroll.core.models.open5e.item_rarity.ItemRarityResponse;
import com.fizzycoyote.qusetroll.core.models.open5e.language.LanguageDao;
import com.fizzycoyote.qusetroll.core.models.open5e.language.LanguageEntity;
import com.fizzycoyote.qusetroll.core.models.open5e.language.LanguageMapper;
import com.fizzycoyote.qusetroll.core.models.open5e.language.LanguageResponse;
import com.fizzycoyote.qusetroll.core.models.open5e.license.LicenseDao;
import com.fizzycoyote.qusetroll.core.models.open5e.license.LicenseEntity;
import com.fizzycoyote.qusetroll.core.models.open5e.license.LicenseMapper;
import com.fizzycoyote.qusetroll.core.models.open5e.license.LicenseResponse;
import com.fizzycoyote.qusetroll.core.models.open5e.publisher.PublisherDao;
import com.fizzycoyote.qusetroll.core.models.open5e.publisher.PublisherEntity;
import com.fizzycoyote.qusetroll.core.models.open5e.publisher.PublisherMapper;
import com.fizzycoyote.qusetroll.core.models.open5e.publisher.PublisherResponse;
import com.fizzycoyote.qusetroll.core.models.open5e.rule.RuleDao;
import com.fizzycoyote.qusetroll.core.models.open5e.rule.RuleDto;
import com.fizzycoyote.qusetroll.core.models.open5e.rule.RuleEntity;
import com.fizzycoyote.qusetroll.core.models.open5e.rule.RuleMapper;
import com.fizzycoyote.qusetroll.core.models.open5e.rule.RuleResponse;
import com.fizzycoyote.qusetroll.core.models.open5e.rule_set.RulesetDao;
import com.fizzycoyote.qusetroll.core.models.open5e.rule_set.RulesetDto;
import com.fizzycoyote.qusetroll.core.models.open5e.rule_set.RulesetEntity;
import com.fizzycoyote.qusetroll.core.models.open5e.rule_set.RulesetMapper;
import com.fizzycoyote.qusetroll.core.models.open5e.rule_set.RulesetResponse;
import com.fizzycoyote.qusetroll.core.models.open5e.service.ServiceDao;
import com.fizzycoyote.qusetroll.core.models.open5e.service.ServiceEntity;
import com.fizzycoyote.qusetroll.core.models.open5e.service.ServiceMapper;
import com.fizzycoyote.qusetroll.core.models.open5e.service.ServiceResponse;
import com.fizzycoyote.qusetroll.core.models.open5e.species.SpeciesDao;
import com.fizzycoyote.qusetroll.core.models.open5e.species.SpeciesEntity;
import com.fizzycoyote.qusetroll.core.models.open5e.species.SpeciesMapper;
import com.fizzycoyote.qusetroll.core.models.open5e.species.SpeciesResponse;
import com.fizzycoyote.qusetroll.core.models.open5e.spell.SpellDao;
import com.fizzycoyote.qusetroll.core.models.open5e.spell.SpellEntity;
import com.fizzycoyote.qusetroll.core.models.open5e.spell.SpellMapper;
import com.fizzycoyote.qusetroll.core.models.open5e.spell_school.SpellSchoolDao;
import com.fizzycoyote.qusetroll.core.models.open5e.spell_school.SpellSchoolEntity;
import com.fizzycoyote.qusetroll.core.models.open5e.spell_school.SpellSchoolMapper;
import com.fizzycoyote.qusetroll.core.models.open5e.spell_school.SpellSchoolResponse;
import com.fizzycoyote.qusetroll.core.models.open5e.spell.SpellResponse;
import com.fizzycoyote.qusetroll.core.models.open5e.weapon_property.WeaponPropertyDao;
import com.fizzycoyote.qusetroll.core.models.open5e.weapon_property.WeaponPropertyEntity;
import com.fizzycoyote.qusetroll.core.models.open5e.weapon_property.WeaponPropertyMapper;
import com.fizzycoyote.qusetroll.core.models.open5e.weapon_property.WeaponPropertyResponse;
import com.fizzycoyote.qusetroll.feature_loading.DataSection;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import retrofit2.Response;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class Open5eRepository {
    private final Open5eApiService api;
    private final PublisherDao publisherDao;
    private final GameSystemDao gameSystemDao;
    private final LicenseDao licenseDao;
    private final DocumentDao documentDao;
    private final LanguageDao languageDao;
    private final AbilityDao abilityDao;
    private final SkillDao skillDao;
    private final CharacterClassDao characterClassDao;
    private final FeatureDao featureDao;
    private final HitPointsDao hitPointsDao;
    private final SavingThrowDao savingThrowDao;
    private final SpellDao spellDao;
    private final SpellSchoolDao spellSchoolDao;
    private final CreatureDao creatureDao;
    private final SpeciesDao speciesDao;
    private final BackgroundDao backgroundDao;
    private final ItemDao itemDao;
    private final DamageTypeDao damageTypeDao;
    private final AlignmentDao alignmentDao;
    private final ItemRarityDao itemRarityDao;
    private final WeaponPropertyDao weaponPropertyDao;
    private final ServiceDao serviceDao;
    private final EnvironmentDao environmentDao;
    private final RuleDao ruleDao;
    private final RulesetDao rulesetDao;
    private final ConditionDao conditionDao;
    private final CreatureTypeDao creatureTypeDao;
    private final ItemCategoryDao itemCategoryDao;
    private final Executor executor;

    public Open5eRepository(Open5eApiService api,
                            PublisherDao publisherDao,
                            GameSystemDao gameSystemDao,
                            LicenseDao licenseDao,
                            DocumentDao documentDao,
                            LanguageDao languageDao,
                            AbilityDao abilityDao,
                            SkillDao skillDao,
                            CharacterClassDao characterClassDao,
                            FeatureDao featureDao,
                            HitPointsDao hitPointsDao,
                            SavingThrowDao savingThrowDao,
                            SpellDao spellDao,
                            SpellSchoolDao spellSchoolDao,
                            CreatureDao creatureDao,
                            SpeciesDao speciesDao,
                            BackgroundDao backgroundDao,
                            ItemDao itemDao,
                            DamageTypeDao damageTypeDao,
                            AlignmentDao alignmentDao,
                            ItemRarityDao itemRarityDao,
                            WeaponPropertyDao weaponPropertyDao,
                            ServiceDao serviceDao,
                            EnvironmentDao environmentDao,
                            RuleDao ruleDao,
                            RulesetDao rulesetDao,
                            ConditionDao conditionDao,
                            CreatureTypeDao creatureTypeDao,
                            ItemCategoryDao itemCategoryDao,
                            Executor executor) {
        this.api = api;
        this.publisherDao = publisherDao;
        this.gameSystemDao = gameSystemDao;
        this.licenseDao = licenseDao;
        this.documentDao = documentDao;
        this.languageDao = languageDao;
        this.abilityDao = abilityDao;
        this.skillDao = skillDao;
        this.characterClassDao = characterClassDao;
        this.featureDao = featureDao;
        this.hitPointsDao = hitPointsDao;
        this.savingThrowDao = savingThrowDao;
        this.spellDao = spellDao;
        this.spellSchoolDao = spellSchoolDao;
        this.creatureDao = creatureDao;
        this.speciesDao = speciesDao;
        this.backgroundDao = backgroundDao;
        this.itemDao = itemDao;
        this.damageTypeDao = damageTypeDao;
        this.alignmentDao = alignmentDao;
        this.itemRarityDao = itemRarityDao;
        this.weaponPropertyDao = weaponPropertyDao;
        this.serviceDao = serviceDao;
        this.environmentDao = environmentDao;
        this.ruleDao = ruleDao;
        this.rulesetDao = rulesetDao;
        this.conditionDao = conditionDao;
        this.creatureTypeDao = creatureTypeDao;
        this.itemCategoryDao = itemCategoryDao;
        this.executor = executor;
    }

    public LiveData<Resource<Boolean>> refreshAllData() {
        MutableLiveData<Resource<Boolean>> result = new MutableLiveData<>();
        result.postValue(Resource.loading(null, 0));

        executor.execute(() -> {
            try {
                int totalSections = DataSection.values().length;
                AtomicInteger completed = new AtomicInteger(0);

                List<CompletableFuture<Void>> futures = new ArrayList<>();

                futures.add(processPublishers(completed, totalSections, result));
                futures.add(processLicenses(completed, totalSections, result));
                futures.add(processDocuments(completed, totalSections, result));
                futures.add(processGameSystems(completed, totalSections, result));
                futures.add(processLanguages(completed, totalSections, result));
                futures.add(processAbilities(completed, totalSections, result));
                futures.add(processCharacterClasses(completed, totalSections, result));
                futures.add(processSpells(completed, totalSections, result));
                futures.add(processSpellSchools(completed, totalSections, result));
                futures.add(processCreatures(completed, totalSections, result));
                futures.add(processSpecies(completed, totalSections, result));
                futures.add(processBackgrounds(completed, totalSections, result));
                futures.add(processItems(completed, totalSections, result));
                futures.add(processDamageTypes(completed, totalSections, result));
                futures.add(processAlignments(completed, totalSections, result));
                futures.add(processItemRarities(completed, totalSections, result));
                futures.add(processWeaponProperties(completed, totalSections, result));
                futures.add(processServices(completed, totalSections, result));
                futures.add(processEnvironments(completed, totalSections, result));
                futures.add(processRules(completed, totalSections, result));
                futures.add(processRulesets(completed, totalSections, result));
                futures.add(processConditions(completed, totalSections, result));
                futures.add(processCreatureTypes(completed, totalSections, result));
                futures.add(processItemCategories(completed, totalSections, result));

                CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                        .thenRun(() -> result.postValue(Resource.success(true)))
                        .exceptionally(ex -> {
                            result.postValue(Resource.error(ex.getMessage(), false));
                            return null;
                        });

            } catch (Exception e) {
                result.postValue(Resource.error("Initialization failed: " + e.getMessage(), false));
            }
        });

        return result;
    }

    public LiveData<Resource<Boolean>> refreshSelectedData(Set<DataSection> sections) {
        MutableLiveData<Resource<Boolean>> result = new MutableLiveData<>();
        result.postValue(Resource.loading(null, 0));

        executor.execute(() -> {
            try {
                int totalSections = sections.size();
                AtomicInteger completed = new AtomicInteger(0);
                List<CompletableFuture<Void>> futures = new ArrayList<>();

                if (sections.contains(DataSection.PUBLISHERS))
                    futures.add(processPublishers(completed, totalSections, result));
                if (sections.contains(DataSection.LICENSES))
                    futures.add(processLicenses(completed, totalSections, result));
                if (sections.contains(DataSection.DOCUMENTS))
                    futures.add(processDocuments(completed, totalSections, result));
                if (sections.contains(DataSection.GAME_SYSTEMS))
                    futures.add(processGameSystems(completed, totalSections, result));
                if (sections.contains(DataSection.LANGUAGES))
                    futures.add(processLanguages(completed, totalSections, result));
                if (sections.contains(DataSection.ABILITIES))
                    futures.add(processAbilities(completed, totalSections, result));
                if (sections.contains(DataSection.CLASSES))
                    futures.add(processCharacterClasses(completed, totalSections, result));
                if (sections.contains(DataSection.SPELLS))
                    futures.add(processSpells(completed, totalSections, result));
                if (sections.contains(DataSection.SPELL_SCHOOLS))
                    futures.add(processSpellSchools(completed, totalSections, result));
                if (sections.contains(DataSection.CREATURES))
                    futures.add(processCreatures(completed, totalSections, result));
                if (sections.contains(DataSection.SPECIES))
                    futures.add(processSpecies(completed, totalSections, result));
                if (sections.contains(DataSection.BACKGROUNDS))
                    futures.add(processBackgrounds(completed, totalSections, result));
                if (sections.contains(DataSection.ITEMS))
                    futures.add(processItems(completed, totalSections, result));
                if (sections.contains(DataSection.DAMAGE_TYPES))
                    futures.add(processDamageTypes(completed, totalSections, result));
                if (sections.contains(DataSection.ALIGNMENTS))
                    futures.add(processAlignments(completed, totalSections, result));
                if (sections.contains(DataSection.ITEM_RARITIES))
                    futures.add(processItemRarities(completed, totalSections, result));
                if (sections.contains(DataSection.WEAPON_PROPERTIES))
                    futures.add(processWeaponProperties(completed, totalSections, result));
                if (sections.contains(DataSection.SERVICES))
                    futures.add(processServices(completed, totalSections, result));
                if (sections.contains(DataSection.ENVIRONMENTS))
                    futures.add(processEnvironments(completed, totalSections, result));
                if (sections.contains(DataSection.RULES))
                    futures.add(processRules(completed, totalSections, result));
                if (sections.contains(DataSection.RULESETS))
                    futures.add(processRulesets(completed, totalSections, result));
                if (sections.contains(DataSection.CONDITIONS))
                    futures.add(processConditions(completed, totalSections, result));
                if (sections.contains(DataSection.CREATURE_TYPES))
                    futures.add(processCreatureTypes(completed, totalSections, result));
                if (sections.contains(DataSection.ITEM_CATEGORIES))
                    futures.add(processItemCategories(completed, totalSections, result));

                CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                        .thenRun(() -> result.postValue(Resource.success(true)))
                        .exceptionally(ex -> {
                            result.postValue(Resource.error(ex.getMessage(), false));
                            return null;
                        });

            } catch (Exception e) {
                result.postValue(Resource.error("Failed: " + e.getMessage(), false));
            }
        });

        return result;
    }

    private CompletableFuture<Void> processItemCategories(AtomicInteger completed, int total,
                                                          MutableLiveData<Resource<Boolean>> result) {
        return CompletableFuture.runAsync(() -> {
            try {
                List<ItemCategoryEntity> all = new ArrayList<>();
                int page = 1;
                boolean hasMore = true;
                int maxRetries = 3;

                while (hasMore) {
                    Response<ItemCategoryResponse> response = null; // ← poprawny typ
                    for (int retry = 0; retry < maxRetries; retry++) {
                        try {
                            response = api.getItemCategoriesPage(page).execute(); // musi zwracać Call<ItemCategoryResponse>
                            if (response.isSuccessful()) break;
                        } catch (Exception e) {
                            Log.w("Repository", "Retry " + retry + " for item categories page " + page);
                            if (retry == maxRetries - 1) throw e;
                            Thread.sleep(2000);
                        }
                    }
                    if (response == null || !response.isSuccessful() || response.body() == null) {
                        Log.e("Repository", "Failed to fetch item categories page " + page);
                        updateProgress(completed, total, result);
                        return;
                    }
                    ItemCategoryResponse body = response.body(); // ← poprawny typ
                    List<ItemCategoryEntity> entities = body.results.stream()
                            .map(ItemCategoryMapper::dtoToEntity)
                            .collect(Collectors.toList());
                    all.addAll(entities);
                    Log.d("Repository", "Item categories page " + page + ": " + entities.size()
                            + " (total: " + all.size() + "/" + body.count + ")");
                    hasMore = body.next != null;
                    page++;
                }
                if (!all.isEmpty()) {
                    itemCategoryDao.deleteAll();
                    itemCategoryDao.insertAll(all);
                    Log.d("Repository", "Saved " + all.size() + " item categories");
                }
                updateProgress(completed, total, result);
            } catch (Exception e) {
                handleError("Item Categories", e);
            }
        }, executor);
    }

    private CompletableFuture<Void> processCreatureTypes(AtomicInteger completed, int total,
                                                         MutableLiveData<Resource<Boolean>> result) {
        return CompletableFuture.runAsync(() -> {
            try {
                List<CreatureTypeEntity> allTypes = new ArrayList<>();
                int page = 1;
                boolean hasMore = true;
                int maxRetries = 3;

                while (hasMore) {
                    Response<CreatureTypeResponse> response = null;
                    for (int retry = 0; retry < maxRetries; retry++) {
                        try {
                            response = api.getCreatureTypesPage(page).execute();
                            if (response.isSuccessful()) break;
                        } catch (Exception e) {
                            Log.w("Repository", "Retry " + retry + " for creature types page " + page);
                            if (retry == maxRetries - 1) throw e;
                            Thread.sleep(2000);
                        }
                    }

                    if (response == null || !response.isSuccessful() || response.body() == null) {
                        Log.e("Repository", "Failed to fetch creature types page " + page);
                        updateProgress(completed, total, result);
                        return;
                    }

                    CreatureTypeResponse body = response.body();
                    List<CreatureTypeEntity> entities = body.results.stream()
                            .map(CreatureTypeMapper::dtoToEntity)
                            .collect(Collectors.toList());
                    allTypes.addAll(entities);
                    Log.d("Repository", "Creature types page " + page + ": " + entities.size()
                            + " (total: " + allTypes.size() + "/" + body.count + ")");
                    hasMore = body.next != null;
                    page++;
                }

                if (!allTypes.isEmpty()) {
                    creatureTypeDao.deleteAll();
                    creatureTypeDao.insertAll(allTypes);
                    Log.d("Repository", "Saved " + allTypes.size() + " creature types");
                }

                updateProgress(completed, total, result);
            } catch (Exception e) {
                handleError("Creature Types", e);
            }
        }, executor);
    }

    private CompletableFuture<Void> processConditions(AtomicInteger completed, int total,
                                                      MutableLiveData<Resource<Boolean>> result) {
        return CompletableFuture.runAsync(() -> {
            try {
                List<ConditionEntity> allConditions = new ArrayList<>();
                int page = 1;
                boolean hasMore = true;
                int maxRetries = 3;

                while (hasMore) {
                    Response<ConditionResponse> response = null;
                    for (int retry = 0; retry < maxRetries; retry++) {
                        try {
                            response = api.getConditionsPage(page).execute();
                            if (response.isSuccessful()) break;
                        } catch (Exception e) {
                            Log.w("Repository", "Retry " + retry + " for conditions page " + page);
                            if (retry == maxRetries - 1) throw e;
                            Thread.sleep(2000);
                        }
                    }

                    if (response == null || !response.isSuccessful() || response.body() == null) {
                        Log.e("Repository", "Failed to fetch conditions page " + page);
                        updateProgress(completed, total, result);
                        return;
                    }

                    ConditionResponse body = response.body();
                    List<ConditionEntity> entities = body.results.stream()
                            .map(ConditionMapper::dtoToEntity)
                            .collect(Collectors.toList());
                    allConditions.addAll(entities);
                    Log.d("Repository", "Conditions page " + page + ": " + entities.size()
                            + " (total: " + allConditions.size() + "/" + body.count + ")");
                    hasMore = body.next != null;
                    page++;
                }

                if (!allConditions.isEmpty()) {
                    conditionDao.deleteAll();
                    conditionDao.insertAll(allConditions);
                    Log.d("Repository", "Saved " + allConditions.size() + " conditions");
                }

                updateProgress(completed, total, result);
            } catch (Exception e) {
                handleError("Conditions", e);
            }
        }, executor);
    }

    private CompletableFuture<Void> processRulesets(AtomicInteger completed, int total,
                                                    MutableLiveData<Resource<Boolean>> result) {
        return CompletableFuture.runAsync(() -> {
            try {
                List<RulesetEntity> allRulesets = new ArrayList<>();
                List<RuleEntity> allRules = new ArrayList<>();
                int page = 1;
                boolean hasMore = true;
                int maxRetries = 3;

                while (hasMore) {
                    Response<RulesetResponse> response = null;
                    for (int retry = 0; retry < maxRetries; retry++) {
                        try {
                            response = api.getRulesetsPage(page).execute();
                            if (response.isSuccessful()) break;
                        } catch (Exception e) {
                            Log.w("Repository", "Retry " + retry + " for rulesets page " + page);
                            if (retry == maxRetries - 1) throw e;
                            Thread.sleep(2000);
                        }
                    }

                    if (response == null || !response.isSuccessful() || response.body() == null) {
                        Log.e("Repository", "Failed to fetch rulesets page " + page);
                        updateProgress(completed, total, result);
                        return;
                    }

                    RulesetResponse body = response.body();
                    for (RulesetDto dto : body.results) {
                        allRulesets.add(RulesetMapper.dtoToEntity(dto));

                        if (dto.rules != null) {
                            for (RuleDto ruleDto : dto.rules) {
                                RuleEntity rule = RuleMapper.dtoToEntity(ruleDto);
                                rule.rulesetKey = dto.key;
                                allRules.add(rule);
                            }
                        }
                    }

                    Log.d("Repository", "Rulesets page " + page + ": " + body.results.size()
                            + " rulesets, total rules so far: " + allRules.size());

                    hasMore = body.next != null;
                    page++;
                }

                if (!allRulesets.isEmpty()) {
                    rulesetDao.deleteAll();
                    rulesetDao.insertAll(allRulesets);
                    Log.d("Repository", "Saved " + allRulesets.size() + " rulesets");
                }
                if (!allRules.isEmpty()) {
                    ruleDao.deleteAll();
                    ruleDao.insertAll(allRules);
                    Log.d("Repository", "Saved " + allRules.size() + " rules");
                }

                updateProgress(completed, total, result);

            } catch (Exception e) {
                handleError("Rulesets", e);
            }
        }, executor);
    }

    private CompletableFuture<Void> processRules(AtomicInteger completed, int total,
                                                 MutableLiveData<Resource<Boolean>> result) {
        return CompletableFuture.runAsync(() -> {
            try {
                List<RuleEntity> allRules = new ArrayList<>();
                int page = 1;
                boolean hasMore = true;
                int maxRetries = 3;
                while (hasMore) {
                    Response<RuleResponse> response = null;
                    for (int retry = 0; retry < maxRetries; retry++) {
                        try {
                            response = api.getRulesPage(page).execute();
                            if (response.isSuccessful()) break;
                        } catch (Exception e) {
                            Log.w("Repository", "Retry " + retry + " for rules page " + page);
                            if (retry == maxRetries - 1) throw e;
                            Thread.sleep(2000);
                        }
                    }
                    if (response == null || !response.isSuccessful() || response.body() == null) {
                        Log.e("Repository", "Failed to fetch rules page " + page);
                        updateProgress(completed, total, result);
                        return;
                    }
                    RuleResponse body = response.body();
                    List<RuleEntity> entities = body.results.stream()
                            .map(RuleMapper::dtoToEntity)
                            .collect(Collectors.toList());
                    allRules.addAll(entities);
                    Log.d("Repository", "Rules page " + page + ": " + entities.size()
                            + " (total: " + allRules.size() + "/" + body.count + ")");
                    hasMore = body.next != null;
                    page++;
                }
                if (!allRules.isEmpty()) {
                    ruleDao.deleteAll();
                    ruleDao.insertAll(allRules);
                    Log.d("Repository", "Saved " + allRules.size() + " rules");
                }
                updateProgress(completed, total, result);
            } catch (Exception e) {
                handleError("Rules", e);
            }
        }, executor);
    }

    private CompletableFuture<Void> processEnvironments(AtomicInteger completed, int total,
                                                        MutableLiveData<Resource<Boolean>> result) {
        return CompletableFuture.runAsync(() -> {
            try {
                List<EnvironmentEntity> allEnvironments = new ArrayList<>();
                int page = 1;
                boolean hasMore = true;
                int maxRetries = 3;

                while (hasMore) {
                    Response<EnvironmentResponse> response = null;
                    for (int retry = 0; retry < maxRetries; retry++) {
                        try {
                            response = api.getEnvironmentsPage(page).execute();
                            if (response.isSuccessful()) break;
                        } catch (Exception e) {
                            Log.w("Repository", "Retry " + retry + " for environments page " + page);
                            if (retry == maxRetries - 1) throw e;
                            Thread.sleep(2000);
                        }
                    }

                    if (response == null || !response.isSuccessful() || response.body() == null) {
                        Log.e("Repository", "Failed to fetch environments page " + page);
                        updateProgress(completed, total, result);
                        return;
                    }

                    EnvironmentResponse body = response.body();
                    List<EnvironmentEntity> entities = body.results.stream()
                            .map(EnvironmentMapper::dtoToEntity)
                            .collect(Collectors.toList());

                    allEnvironments.addAll(entities);
                    Log.d("Repository", "Environments page " + page + ": " + entities.size()
                            + " (total: " + allEnvironments.size() + "/" + body.count + ")");

                    hasMore = body.next != null;
                    page++;
                }

                if (!allEnvironments.isEmpty()) {
                    environmentDao.deleteAll();
                    environmentDao.insertAll(allEnvironments);
                    Log.d("Repository", "Saved " + allEnvironments.size() + " environments");
                }

                updateProgress(completed, total, result);

            } catch (Exception e) {
                handleError("Environments", e);
            }
        }, executor);
    }

    private CompletableFuture<Void> processServices(AtomicInteger completed, int total,
                                                    MutableLiveData<Resource<Boolean>> result) {
        return CompletableFuture.runAsync(() -> {
            try {
                Response<ServiceResponse> response = api.getServices().execute();
                if (!response.isSuccessful() || response.body() == null) {
                    Log.e("Repository", "Failed to fetch services");
                    updateProgress(completed, total, result);
                    return;
                }
                List<ServiceEntity> entities = response.body().results.stream()
                        .map(ServiceMapper::dtoToEntity)
                        .collect(Collectors.toList());

                serviceDao.deleteAll();
                serviceDao.insertAll(entities);
                Log.d("Repository", "Saved " + entities.size() + " services");
                updateProgress(completed, total, result);
            } catch (Exception e) {
                handleError("Services", e);
            }
        }, executor);
    }

    private CompletableFuture<Void> processWeaponProperties(AtomicInteger completed, int total,
                                                            MutableLiveData<Resource<Boolean>> result) {
        return CompletableFuture.runAsync(() -> {
            try {
                Response<WeaponPropertyResponse> response = api.getWeaponProperties().execute();
                if (!response.isSuccessful() || response.body() == null) {
                    Log.e("Repository", "Failed to fetch weapon properties");
                    updateProgress(completed, total, result);
                    return;
                }
                List<WeaponPropertyEntity> entities = response.body().results.stream()
                        .map(WeaponPropertyMapper::dtoToEntity)
                        .collect(Collectors.toList());

                weaponPropertyDao.deleteAll();
                weaponPropertyDao.insertAll(entities);
                Log.d("Repository", "Saved " + entities.size() + " weapon properties");
                updateProgress(completed, total, result);
            } catch (Exception e) {
                handleError("Weapon Properties", e);
            }
        }, executor);
    }

    private CompletableFuture<Void> processItemRarities(AtomicInteger completed, int total,
                                                        MutableLiveData<Resource<Boolean>> result) {
        return CompletableFuture.runAsync(() -> {
            try {
                Response<ItemRarityResponse> response = api.getItemRarities().execute();
                if (!response.isSuccessful() || response.body() == null) {
                    Log.e("Repository", "Failed to fetch item rarities");
                    updateProgress(completed, total, result);
                    return;
                }
                List<ItemRarityEntity> entities = response.body().results.stream()
                        .map(ItemRarityMapper::dtoToEntity)
                        .collect(Collectors.toList());

                itemRarityDao.deleteAll();
                itemRarityDao.insertAll(entities);
                Log.d("Repository", "Saved " + entities.size() + " item rarities");
                updateProgress(completed, total, result);
            } catch (Exception e) {
                handleError("Item Rarities", e);
            }
        }, executor);
    }

    private CompletableFuture<Void> processAlignments(AtomicInteger completed, int total,
                                                      MutableLiveData<Resource<Boolean>> result) {
        return CompletableFuture.runAsync(() -> {
            try {
                List<AlignmentEntity> allAlignments = new ArrayList<>();
                int page = 1;
                boolean hasMore = true;
                int maxRetries = 3;

                while (hasMore) {
                    Response<AlignmentResponse> response = null;
                    for (int retry = 0; retry < maxRetries; retry++) {
                        try {
                            response = api.getAlignmentsPage(page).execute();
                            if (response.isSuccessful()) break;
                        } catch (Exception e) {
                            Log.w("Repository", "Retry " + retry + " for alignments page " + page);
                            if (retry == maxRetries - 1) throw e;
                            Thread.sleep(2000);
                        }
                    }

                    if (response == null || !response.isSuccessful() || response.body() == null) {
                        Log.e("Repository", "Failed to fetch alignments page " + page);
                        updateProgress(completed, total, result);
                        return;
                    }

                    AlignmentResponse body = response.body();
                    List<AlignmentEntity> entities = body.results.stream()
                            .map(AlignmentMapper::dtoToEntity)
                            .collect(Collectors.toList());

                    allAlignments.addAll(entities);
                    Log.d("Repository", "Alignments page " + page + ": " + entities.size()
                            + " (total: " + allAlignments.size() + "/" + body.count + ")");

                    hasMore = body.next != null;
                    page++;
                }

                if (!allAlignments.isEmpty()) {
                    alignmentDao.deleteAll();
                    alignmentDao.insertAll(allAlignments);
                    Log.d("Repository", "Saved " + allAlignments.size() + " alignments");
                }

                updateProgress(completed, total, result);

            } catch (Exception e) {
                handleError("Alignments", e);
            }
        }, executor);
    }

    private CompletableFuture<Void> processDamageTypes(AtomicInteger completed, int total,
                                                     MutableLiveData<Resource<Boolean>> result) {
        return CompletableFuture.runAsync(() -> {
            try {
                List<DamageTypeEntity> allTypes = new ArrayList<>();
                int page = 1;
                boolean hasMore = true;
                int maxRetries = 3;

                while (hasMore) {
                    Response<DamageTypeResponse> response = null;
                    for (int retry = 0; retry < maxRetries; retry++) {
                        try {
                            response = api.getDamageTypesPage(page).execute();
                            if (response.isSuccessful()) break;
                        } catch (Exception e) {
                            Log.w("Repository", "Retry " + retry + " for damage types page " + page);
                            if (retry == maxRetries - 1) throw e;
                            Thread.sleep(2000);
                        }
                    }

                    if (response == null || !response.isSuccessful() || response.body() == null) {
                        Log.e("Repository", "Failed to fetch damage types page " + page);
                        updateProgress(completed, total, result);
                        return;
                    }

                    DamageTypeResponse body = response.body();
                    List<DamageTypeEntity> entities = body.results.stream()
                            .map(DamageTypeMapper::dtoToEntity)
                            .collect(Collectors.toList());

                    allTypes.addAll(entities);
                    Log.d("Repository", "Damage types page " + page + ": " + entities.size()
                            + " (total: " + allTypes.size() + "/" + body.count + ")");

                    hasMore = body.next != null;
                    page++;
                }

                if (!allTypes.isEmpty()) {
                    damageTypeDao.deleteAll();
                    damageTypeDao.insertAll(allTypes);
                    Log.d("Repository", "Saved " + allTypes.size() + " damage types");
                }

                updateProgress(completed, total, result);

            } catch (Exception e) {
                handleError("Damage Types", e);
            }
        }, executor);
    }

    private CompletableFuture<Void> processItems(AtomicInteger completed, int total,
                                                 MutableLiveData<Resource<Boolean>> result) {
        return CompletableFuture.runAsync(() -> {
            try {
                List<ItemEntity> allItems = new ArrayList<>();
                int page = 1;
                boolean hasMore = true;
                int maxRetries = 3;

                while (hasMore) {
                    Response<ItemResponse> response = null;

                    for (int retry = 0; retry < maxRetries; retry++) {
                        try {
                            response = api.getItemsPage(page).execute();
                            if (response.isSuccessful()) break;
                        } catch (Exception e) {
                            Log.w("Repository", "Retry " + retry + " for items page " + page);
                            if (retry == maxRetries - 1) throw e;
                            Thread.sleep(2000); // wait before retry
                        }
                    }

                    if (response == null || !response.isSuccessful() || response.body() == null) {
                        Log.e("Repository", "❌ Failed to fetch items page " + page);
                        updateProgress(completed, total, result);
                        return;
                    }

                    ItemResponse body = response.body();
                    List<ItemEntity> entities = body.results.stream()
                            .map(ItemMapper::dtoToEntity)
                            .collect(Collectors.toList());

                    allItems.addAll(entities);
                    Log.d("Repository", "✅ Items page " + page + ": " + entities.size()
                            + " (total: " + allItems.size() + "/" + body.count + ")");

                    hasMore = body.next != null;
                    page++;
                }

                if (!allItems.isEmpty()) {
                    itemDao.deleteAll();
                    itemDao.insertAll(allItems);
                    Log.d("Repository", "✅ Saved " + allItems.size() + " items");
                }

                updateProgress(completed, total, result);

            } catch (Exception e) {
                handleError("Items", e);
            }
        }, executor);
    }

    private CompletableFuture<Void> processBackgrounds(AtomicInteger completed, int total,
                                                       MutableLiveData<Resource<Boolean>> result) {
        return CompletableFuture.runAsync(() -> {
            try {
                List<BackgroundEntity> allBackgrounds = new ArrayList<>();
                int page = 1;
                boolean hasMore = true;
                int maxRetries = 3;

                while (hasMore) {
                    Response<BackgroundResponse> response = null;

                    for (int retry = 0; retry < maxRetries; retry++) {
                        try {
                            response = api.getBackgroundsPage(page).execute();
                            if (response.isSuccessful()) break;
                        } catch (Exception e) {
                            Log.w("Repository", "Retry " + retry + " for backgrounds page " + page);
                            if (retry == maxRetries - 1) throw e;
                            Thread.sleep(2000);
                        }
                    }

                    if (response == null || !response.isSuccessful() || response.body() == null) {
                        Log.e("Repository", "❌ Failed to fetch backgrounds page " + page);
                        updateProgress(completed, total, result);
                        return;
                    }

                    BackgroundResponse body = response.body();
                    List<BackgroundEntity> entities = body.results.stream()
                            .map(BackgroundMapper::dtoToEntity)
                            .collect(Collectors.toList());

                    allBackgrounds.addAll(entities);
                    Log.d("Repository", "✅ Backgrounds page " + page + ": " + entities.size()
                            + " (total: " + allBackgrounds.size() + "/" + body.count + ")");

                    hasMore = body.next != null;
                    page++;
                }

                if (!allBackgrounds.isEmpty()) {
                    backgroundDao.deleteAll();
                    backgroundDao.insertAll(allBackgrounds);
                    Log.d("Repository", "✅ Saved " + allBackgrounds.size() + " backgrounds");
                }

                updateProgress(completed, total, result);

            } catch (Exception e) {
                handleError("Backgrounds", e);
            }
        }, executor);
    }

    private CompletableFuture<Void> processSpecies(AtomicInteger completed, int total,
                                                   MutableLiveData<Resource<Boolean>> result) {
        return CompletableFuture.runAsync(() -> {
            try {
                List<SpeciesEntity> allSpecies = new ArrayList<>();
                int page = 1;
                boolean hasMore = true;
                int maxRetries = 3;

                while (hasMore) {
                    Response<SpeciesResponse> response = null;

                    for (int retry = 0; retry < maxRetries; retry++) {
                        try {
                            response = api.getSpeciesPage(page).execute();
                            if (response.isSuccessful()) break;
                        } catch (Exception e) {
                            Log.w("Repository", "Retry " + retry + " for species page " + page);
                            if (retry == maxRetries - 1) throw e;
                            Thread.sleep(2000);
                        }
                    }

                    if (response == null || !response.isSuccessful() || response.body() == null) {
                        Log.e("Repository", "❌ Failed to fetch species page " + page);
                        updateProgress(completed, total, result);
                        return;
                    }

                    SpeciesResponse body = response.body();
                    List<SpeciesEntity> entities = body.results.stream()
                            .map(SpeciesMapper::dtoToEntity)
                            .collect(Collectors.toList());

                    allSpecies.addAll(entities);
                    Log.d("Repository", "✅ Species page " + page + ": " + entities.size()
                            + " (total: " + allSpecies.size() + "/" + body.count + ")");

                    hasMore = body.next != null;
                    page++;
                }

                if (!allSpecies.isEmpty()) {
                    speciesDao.deleteAll();
                    speciesDao.insertAll(allSpecies);
                    Log.d("Repository", "✅ Saved " + allSpecies.size() + " species");
                }

                updateProgress(completed, total, result);

            } catch (Exception e) {
                handleError("Species", e);
            }
        }, executor);
    }

    private CompletableFuture<Void> processCreatures(AtomicInteger completed, int total,
                                                     MutableLiveData<Resource<Boolean>> result) {
        return CompletableFuture.runAsync(() -> {
            try {
                List<CreatureEntity> allCreatures = new ArrayList<>();
                int page = 1;
                boolean hasMore = true;
                int maxRetries = 3;

                while (hasMore) {
                    Response<CreatureResponse> response = null;

                    for (int retry = 0; retry < maxRetries; retry++) {
                        try {
                            response = api.getCreaturesPage(page).execute();
                            if (response.isSuccessful()) break;
                        } catch (Exception e) {
                            Log.w("Repository", "Retry " + retry + " for creatures page " + page);
                            if (retry == maxRetries - 1) throw e;
                            Thread.sleep(2000);
                        }
                    }

                    if (response == null || !response.isSuccessful() || response.body() == null) {
                        Log.e("Repository", "❌ Failed to fetch creatures page " + page);
                        updateProgress(completed, total, result);
                        return;
                    }

                    CreatureResponse body = response.body();
                    List<CreatureEntity> entities = body.results.stream()
                            .map(CreatureMapper::dtoToEntity)
                            .collect(Collectors.toList());

                    allCreatures.addAll(entities);
                    Log.d("Repository", "✅ Creatures page " + page + ": " + entities.size()
                            + " (total: " + allCreatures.size() + "/" + body.count + ")");

                    hasMore = body.next != null;
                    page++;
                }

                if (allCreatures.size() > 100) {
                    creatureDao.deleteAll();
                    creatureDao.insertAll(allCreatures);
                    Log.d("Repository", "✅ Saved " + allCreatures.size() + " creatures");
                } else {
                    Log.w("Repository", "⚠️ Too few creatures (" + allCreatures.size() + "), skipping");
                }

                updateProgress(completed, total, result);

            } catch (Exception e) {
                handleError("Creatures", e);
            }
        }, executor);
    }

    private CompletableFuture<Void> processSpellSchools(AtomicInteger completed, int total,
                                                        MutableLiveData<Resource<Boolean>> result) {
        return CompletableFuture.runAsync(() -> {
            try {
                Response<SpellSchoolResponse> response = api.getSpellSchools().execute();
                if (response.isSuccessful() && response.body() != null) {
                    List<SpellSchoolEntity> entities = response.body().getResults().stream()
                            .map(SpellSchoolMapper::dtoToEntity)
                            .collect(Collectors.toList());
                    spellSchoolDao.insertAll(entities);
                    Log.d("Repository", "✅ Saved " + entities.size() + " spell schools");
                }
                updateProgress(completed, total, result);
            } catch (Exception e) {
                handleError("Spell Schools", e);
            }
        }, executor);
    }

    private CompletableFuture<Void> processSpells(AtomicInteger completed, int total,
                                                  MutableLiveData<Resource<Boolean>> result) {
        return CompletableFuture.runAsync(() -> {
            try {
                spellDao.deleteAll();

                int page = 1;
                boolean hasMore = true;
                int totalSaved = 0;

                while (hasMore) {
                    Response<SpellResponse> response = api.getSpellsPage(page).execute();
                    if (!response.isSuccessful() || response.body() == null) break;

                    SpellResponse body = response.body();
                    List<SpellEntity> entities = body.getResults().stream()
                            .map(SpellMapper::dtoToEntity)
                            .collect(Collectors.toList());

                    spellDao.insertAll(entities);
                    totalSaved += entities.size();

                    hasMore = body.next != null;
                    page++;
                }

                updateProgress(completed, total, result);

            } catch (Exception e) {
                handleError("Spells", e);
            }
        }, executor);
    }

    private CompletableFuture<Void> processPublishers(AtomicInteger completed, int total, MutableLiveData<Resource<Boolean>> result) {
        return CompletableFuture.runAsync(() -> {
            try {
                Response<PublisherResponse> response = api.getPublishers().execute();
                List<PublisherEntity> entities = response.body().getResults().stream()
                        .map(PublisherMapper::dtoToEntity)
                        .collect(Collectors.toList());

                publisherDao.insertAll(entities);
                updateProgress(completed, total, result);

            } catch (Exception e) {
                handleError("Publishers", e);
            }
        }, executor);
    }

    private CompletableFuture<Void> processLicenses(AtomicInteger completed, int total, MutableLiveData<Resource<Boolean>> result) {
        return CompletableFuture.runAsync(() -> {
            try {
                Response<LicenseResponse> response = api.getLicenses().execute();
                List<LicenseEntity> entities = response.body().getResults().stream()
                        .map(LicenseMapper::dtoToEntity)
                        .collect(Collectors.toList());

                licenseDao.insertAll(entities);
                updateProgress(completed, total, result);

            } catch (Exception e) {
                handleError("Licenses", e);
            }
        }, executor);
    }

    private CompletableFuture<Void> processDocuments(AtomicInteger completed, int total, MutableLiveData<Resource<Boolean>> result) {
        return CompletableFuture.runAsync(() -> {
            try {
                Response<DocumentResponse> response = api.getDocuments().execute();
                List<DocumentEntity> entities = response.body().getResults().stream()
                        .map(DocumentMapper::dtoToEntity)
                        .collect(Collectors.toList());

                documentDao.insertAll(entities);
                updateProgress(completed, total, result);

            } catch (Exception e) {
                handleError("Documents", e);
            }
        }, executor);
    }

    private CompletableFuture<Void> processGameSystems(AtomicInteger completed, int total, MutableLiveData<Resource<Boolean>> result) {
        return CompletableFuture.runAsync(() -> {
            try {
                Response<GameSystemResponse> response = api.getGameSystems().execute();
                List<GameSystemEntity> entities = response.body().getResults().stream()
                        .map(GameSystemMapper::dtoToEntity)
                        .collect(Collectors.toList());

                gameSystemDao.insertAll(entities);
                updateProgress(completed, total, result);

            } catch (Exception e) {
                handleError("Game Systems", e);
            }
        }, executor);
    }

    private CompletableFuture<Void> processLanguages(AtomicInteger completed, int total, MutableLiveData<Resource<Boolean>> result) {
        return CompletableFuture.runAsync(() -> {
            try {
                Response<LanguageResponse> response = api.getLanguages().execute();
                List<LanguageEntity> entities = response.body().getResults().stream()
                        .map(LanguageMapper::dtoToEntity)
                        .collect(Collectors.toList());

                languageDao.insertAll(entities);
                updateProgress(completed, total, result);

            } catch (Exception e) {
                handleError("Languages", e);
            }
        }, executor);
    }

    private CompletableFuture<Void> processAbilities(AtomicInteger completed, int total,
                                                     MutableLiveData<Resource<Boolean>> result) {
        return CompletableFuture.runAsync(() -> {
            try {
                List<AbilityEntity> allAbilities = new ArrayList<>();
                List<SkillEntity>   allSkills    = new ArrayList<>();
                int page = 1;
                boolean hasMore = true;
                int maxRetries = 3;

                while (hasMore) {
                    Response<AbilityResponse> response = null;
                    for (int retry = 0; retry < maxRetries; retry++) {
                        try {
                            response = api.getAbilitiesPage(page).execute();
                            if (response.isSuccessful()) break;
                        } catch (Exception e) {
                            if (retry == maxRetries - 1) throw e;
                            Thread.sleep(2000);
                        }
                    }

                    if (response == null || !response.isSuccessful() || response.body() == null) {
                        updateProgress(completed, total, result);
                        return;
                    }

                    AbilityResponse body = response.body();
                    for (AbilityDto dto : body.results) {
                        allAbilities.add(AbilityMapper.dtoToEntity(dto));
                        allSkills.addAll(AbilityMapper.dtosToSkillEntities(dto));
                    }

                    hasMore = body.next != null;
                    page++;
                }

                if (!allAbilities.isEmpty()) {
                    skillDao.deleteAll();   // delete skills first (FK)
                    abilityDao.deleteAll();
                    abilityDao.insertAll(allAbilities);
                    skillDao.insertAll(allSkills);
                }

                updateProgress(completed, total, result);

            } catch (Exception e) {
                handleError("Abilities", e);
            }
        }, executor);
    }

    private CompletableFuture<Void> processCharacterClasses(AtomicInteger completed, int total, MutableLiveData<Resource<Boolean>> result) {
        return CompletableFuture.runAsync(() -> {
            try {
                Response<CharacterClassResponse> response = api.getCharacterClasses().execute();
                if (response.isSuccessful() && response.body() != null) {
                    List<CharacterClassDto> dtos = response.body().getResults();


                    // ignore srd-2024 becuase its empty
                    // TODO when api will change update thats !
                    List<CharacterClassDto> filteredDtos = dtos.stream()
                            .filter(dto -> dto.document == null || !"srd-2024".equals(dto.document.key))
                            .collect(Collectors.toList());

                    executor.execute(() -> {
                        for (CharacterClassDto dto : filteredDtos) {
                            processSingleClass(dto);
                        }
                    });

                    updateProgress(completed, total, result);
                }
            } catch (Exception e) {
                handleError("Character Classes", e);
            }
        }, executor);
    }

    private void processSingleClass(CharacterClassDto dto) {
        Log.d("Repository", "🔄 Processing class: " + dto.name);

        // another security
        //TODO when api will change update thats !
        if (dto.document != null && "srd-2024".equals(dto.document.key)) {
            return;
        }

        characterClassDao.deleteClass(dto.key);
        featureDao.deleteFeaturesForClass(dto.key);
        hitPointsDao.deleteHitPointsForClass(dto.key);
        savingThrowDao.deleteSavingThrowsForClass(dto.key);

        CharacterClassEntity classEntity = CharacterClassMapper.toClassEntity(dto);
        characterClassDao.insertClass(classEntity);

        if(dto.hitPoints != null) {
            HitPointsEntity hpEntity = CharacterClassMapper.toHitPointsEntity(dto);
            hitPointsDao.insertHitPoints(hpEntity);
        }

        if(dto.features != null && !dto.features.isEmpty()) {
            List<FeatureEntity> features = CharacterClassMapper.toFeatureEntities(dto.key, dto.features);


            int featuresWithTableData = 0;
            int totalTableEntries = 0;

            for (FeatureEntity feature : features) {
                if (feature.tableData != null && !feature.tableData.isEmpty()) {
                    featuresWithTableData++;
                    totalTableEntries += feature.tableData.size();

                    for (int i = 0; i < Math.min(3, feature.tableData.size()); i++) {
                        TableData td = feature.tableData.get(i);
                    }
                }
            }
            featureDao.insertFeatures(features);
        }

        if(dto.savingThrows != null && !dto.savingThrows.isEmpty()) {
            List<SavingThrowEntity> savingThrows = CharacterClassMapper.mapSavingThrows(dto);
            savingThrowDao.insertAll(savingThrows);
        }
    }

    private void updateProgress(AtomicInteger completed, int total, MutableLiveData<Resource<Boolean>> result) {
        int progress = (int) ((completed.incrementAndGet() / (double) total) * 100);
        result.postValue(Resource.loading(null, progress));
    }

    private void handleError(String sectionName, Exception e) {
        Log.e("Repository", "Error loading " + sectionName, e);
        throw new CompletionException(e);
    }

}