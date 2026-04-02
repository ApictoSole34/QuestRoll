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
import com.fizzycoyote.qusetroll.core.models.open5e.creature.CreatureDao;
import com.fizzycoyote.qusetroll.core.models.open5e.creature.CreatureEntity;
import com.fizzycoyote.qusetroll.core.models.open5e.creature.CreatureMapper;
import com.fizzycoyote.qusetroll.core.models.open5e.creature.CreatureResponse;
import com.fizzycoyote.qusetroll.core.models.open5e.damage_type.DamageTypeDao;
import com.fizzycoyote.qusetroll.core.models.open5e.damage_type.DamageTypeEntity;
import com.fizzycoyote.qusetroll.core.models.open5e.damage_type.DamageTypeMapper;
import com.fizzycoyote.qusetroll.core.models.open5e.damage_type.DamageTypeResponse;
import com.fizzycoyote.qusetroll.core.models.open5e.document.DocumentDao;
import com.fizzycoyote.qusetroll.core.models.open5e.document.DocumentEntity;
import com.fizzycoyote.qusetroll.core.models.open5e.document.DocumentMapper;
import com.fizzycoyote.qusetroll.core.models.open5e.document.DocumentResponse;
import com.fizzycoyote.qusetroll.core.models.open5e.game_system.GameSystemDao;
import com.fizzycoyote.qusetroll.core.models.open5e.game_system.GameSystemEntity;
import com.fizzycoyote.qusetroll.core.models.open5e.game_system.GameSystemMapper;
import com.fizzycoyote.qusetroll.core.models.open5e.game_system.GameSystemResponse;
import com.fizzycoyote.qusetroll.core.models.open5e.item.ItemDao;
import com.fizzycoyote.qusetroll.core.models.open5e.item.ItemEntity;
import com.fizzycoyote.qusetroll.core.models.open5e.item.ItemMapper;
import com.fizzycoyote.qusetroll.core.models.open5e.item.ItemResponse;
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