package com.fizzycoyote.qusetroll.core.repository.open5e;

import android.util.Log;

import com.fizzycoyote.qusetroll.core.api.Open5eApiService;
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
    private final Executor executor;

    public Open5eRepository(Open5eApiService api,
                            PublisherDao publisherDao,
                            GameSystemDao gameSystemDao,
                            LicenseDao licenseDao,
                            DocumentDao documentDao,
                            LanguageDao languageDao,
                            Executor executor) {
        this.api = api;
        this.publisherDao = publisherDao;
        this.gameSystemDao = gameSystemDao;
        this.licenseDao = licenseDao;
        this.documentDao = documentDao;
        this.languageDao = languageDao;
        this.executor = executor;
    }

    public LiveData<Resource<Boolean>> refreshAllData() {
        MutableLiveData<Resource<Boolean>> result = new MutableLiveData<>();
        result.postValue(Resource.loading(null, 0));

        executor.execute(() -> {
            try {
                int totalSections = 5;
                AtomicInteger completed = new AtomicInteger(0);

                List<CompletableFuture<Void>> futures = new ArrayList<>();

                futures.add(processPublishers(completed, totalSections, result));
                futures.add(processLicenses(completed, totalSections, result));
                futures.add(processDocuments(completed, totalSections, result));
                futures.add(processGameSystems(completed, totalSections, result));
                futures.add(processLanguages(completed, totalSections, result));

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

    private void updateProgress(AtomicInteger completed, int total, MutableLiveData<Resource<Boolean>> result) {
        int progress = (int) ((completed.incrementAndGet() / (double) total) * 100);
        result.postValue(Resource.loading(null, progress));
    }

    private void handleError(String sectionName, Exception e) {
        Log.e("Repository", "Error loading " + sectionName, e);
        throw new CompletionException(e);
    }

    private DocumentEntity mapDocumentDtoToEntity(DocumentDto dto) {
        DocumentEntity entity = new DocumentEntity();
        entity.key = dto.key;
        entity.url = dto.url;
        List<String> keys = new ArrayList<>();
        for (LicenseDto license : dto.licenses) {
            keys.add(license.key);
        }
        entity.licenses = keys;
        entity.publisher = dto.publisher;
        entity.gamesystem = dto.gamesystem;
        entity.name = dto.name;
        entity.desc = dto.desc;
        entity.author = dto.author;
        entity.publishedAt = dto.publishedAt;
        entity.permalink = dto.permalink;
        entity.distanceUnit = dto.distanceUnit;
        return entity;
    }
}