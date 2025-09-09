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
import com.fizzycoyote.qusetroll.core.models.open5e.document.DocumentDao;
import com.fizzycoyote.qusetroll.core.models.open5e.document.DocumentDto;
import com.fizzycoyote.qusetroll.core.models.open5e.document.DocumentEntity;
import com.fizzycoyote.qusetroll.core.models.open5e.document.DocumentMapper;
import com.fizzycoyote.qusetroll.core.models.open5e.document.DocumentResponse;
import com.fizzycoyote.qusetroll.core.models.open5e.game_system.GameSystemDao;
import com.fizzycoyote.qusetroll.core.models.open5e.game_system.GameSystemEntity;
import com.fizzycoyote.qusetroll.core.models.open5e.game_system.GameSystemMapper;
import com.fizzycoyote.qusetroll.core.models.open5e.game_system.GameSystemResponse;
import com.fizzycoyote.qusetroll.core.models.open5e.language.LanguageDao;
import com.fizzycoyote.qusetroll.core.models.open5e.language.LanguageEntity;
import com.fizzycoyote.qusetroll.core.models.open5e.language.LanguageMapper;
import com.fizzycoyote.qusetroll.core.models.open5e.language.LanguageResponse;
import com.fizzycoyote.qusetroll.core.models.open5e.license.LicenseDao;
import com.fizzycoyote.qusetroll.core.models.open5e.license.LicenseDto;
import com.fizzycoyote.qusetroll.core.models.open5e.license.LicenseEntity;
import com.fizzycoyote.qusetroll.core.models.open5e.license.LicenseMapper;
import com.fizzycoyote.qusetroll.core.models.open5e.license.LicenseResponse;
import com.fizzycoyote.qusetroll.core.models.open5e.publisher.PublisherDao;
import com.fizzycoyote.qusetroll.core.models.open5e.publisher.PublisherEntity;
import com.fizzycoyote.qusetroll.core.models.open5e.publisher.PublisherMapper;
import com.fizzycoyote.qusetroll.core.models.open5e.publisher.PublisherResponse;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import retrofit2.Response;

import java.util.ArrayList;
import java.util.List;
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
        this.executor = executor;
    }

    public LiveData<Resource<Boolean>> refreshAllData() {
        MutableLiveData<Resource<Boolean>> result = new MutableLiveData<>();
        result.postValue(Resource.loading(null, 0));

        executor.execute(() -> {
            try {
                int totalSections = 7;
                AtomicInteger completed = new AtomicInteger(0);

                List<CompletableFuture<Void>> futures = new ArrayList<>();

                futures.add(processPublishers(completed, totalSections, result));
                futures.add(processLicenses(completed, totalSections, result));
                futures.add(processDocuments(completed, totalSections, result));
                futures.add(processGameSystems(completed, totalSections, result));
                futures.add(processLanguages(completed, totalSections, result));
                futures.add(processAbilities(completed, totalSections, result));
                futures.add(processCharacterClasses(completed, totalSections, result));

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

    private CompletableFuture<Void> processAbilities(AtomicInteger completed, int total, MutableLiveData<Resource<Boolean>> result) {
        return CompletableFuture.runAsync(() -> {
            try {
                Response<AbilityResponse> response = api.getAbilities().execute();
                List<AbilityDto> dtos = response.body().getResults();

                List<AbilityEntity> abilities = dtos.stream()
                        .map(AbilityMapper::toEntity)
                        .collect(Collectors.toList());

                List<SkillEntity> skills = dtos.stream()
                        .flatMap(dto -> AbilityMapper.toSkillEntities(dto).stream())
                        .collect(Collectors.toList());

                Executors.newSingleThreadExecutor().execute(() -> {
                    abilityDao.insertAbilities(abilities);
                    skillDao.insertSkills(skills);
                });

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

    private void updateProgress(AtomicInteger completed, int total, MutableLiveData<Resource<Boolean>> result) {
        int progress = (int) ((completed.incrementAndGet() / (double) total) * 100);
        result.postValue(Resource.loading(null, progress));
    }

    private void handleError(String sectionName, Exception e) {
        Log.e("Repository", "Error loading " + sectionName, e);
        throw new CompletionException(e);
    }


    private void processSingleClass(CharacterClassDto dto) {

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
            featureDao.insertFeatures(features);
        }

        if(dto.savingThrows != null && !dto.savingThrows.isEmpty()) {
            List<SavingThrowEntity> savingThrows = CharacterClassMapper.mapSavingThrows(dto);
            savingThrowDao.insertSavingThrows(savingThrows);
        }
    }

    public LiveData<List<CharacterClassEntity>> getBaseClasses() {
        return characterClassDao.getBaseClasses();
    }

    public LiveData<List<CharacterClassEntity>> getSubclasses(String parentKey) {
        return characterClassDao.getSubclasses(parentKey);
    }

    public LiveData<Resource<Integer>> getClassCount() {
        MutableLiveData<Resource<Integer>> result = new MutableLiveData<>();
        executor.execute(() -> {
            try {
                int count = characterClassDao.getClassCount();
                result.postValue(Resource.success(count));
            } catch (Exception e) {
                result.postValue(Resource.error("Count error", 0));
            }
        });
        return result;
    }

    private DocumentEntity mapDocumentDtoToEntity(DocumentDto dto) {
        DocumentEntity entity = new DocumentEntity();
        entity.key = dto.key;
        entity.url = dto.url;
        List<String> keys = new ArrayList<>();
        for (LicenseDto license : dto.licenses) {
            keys.add(license.key);
        }

        entity.publisher = dto.publisher != null ? dto.publisher.key : null;
        entity.gamesystem = dto.gamesystem != null ? dto.gamesystem.key : null;
 
        entity.licenses = keys;
        entity.name = dto.name;
        entity.desc = dto.desc;
        entity.author = dto.author;
        entity.publishedAt = dto.publishedAt;
        entity.permalink = dto.permalink;
        entity.distanceUnit = dto.distanceUnit;
        return entity;
    }
}