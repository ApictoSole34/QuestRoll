package com.fizzycoyote.qusetroll.core.repository.open5e;

import android.util.Log;

import com.fizzycoyote.qusetroll.core.api.Open5eApiService;
import com.fizzycoyote.qusetroll.core.models.open5e.document.DocumentDao;
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
import com.fizzycoyote.qusetroll.core.models.open5e.license.LicenseEntity;
import com.fizzycoyote.qusetroll.core.models.open5e.license.LicenseMapper;
import com.fizzycoyote.qusetroll.core.models.open5e.license.LicenseResponse;
import com.fizzycoyote.qusetroll.core.models.open5e.publisher.PublisherDao;
import com.fizzycoyote.qusetroll.core.models.open5e.publisher.PublisherEntity;
import com.fizzycoyote.qusetroll.core.models.open5e.publisher.PublisherMapper;
import com.fizzycoyote.qusetroll.core.models.open5e.publisher.PublisherResponse;


import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import retrofit2.Response;

public class Open5eRepository {
    private final Open5eApiService api;
    private final DocumentDao documentDao;
    private final GameSystemDao gameSystemDao;
    private final LicenseDao licenseDao;
    private final LanguageDao languageDao;
    private final PublisherDao publisherDao;

    public Open5eRepository(Open5eApiService api,
                            PublisherDao publisherDao,
                            GameSystemDao gameSystemDao,
                            LicenseDao licenseDao,
                            DocumentDao documentDao,
                            LanguageDao languageDao) {
        this.api = api;
        this.publisherDao = publisherDao;
        this.gameSystemDao = gameSystemDao;
        this.licenseDao = licenseDao;
        this.documentDao = documentDao;
        this.languageDao = languageDao;
    }

    public void refreshAllData() {
            try {
                // publishers
                Response<PublisherResponse> pubResp = api.getPublishers().execute();
                Log.d("Open5eRepository", "publishers refreshed" + pubResp.code());
                if (pubResp.isSuccessful() && pubResp.body() != null) {
                    Log.d("Open5eRepository", "publishers refreshed" + pubResp.body().results.size());
                    List<PublisherEntity> pubs = pubResp.body().results.stream()
                                    .map(PublisherMapper::dtoToEntity)
                                    .collect(Collectors.toList());
                    publisherDao.insertAll(pubs);
                } else {
                    Log.d("Open5eRepository", "publishers not refreshed" + pubResp.errorBody().string());
                }

                // gamesystems
                Response<GameSystemResponse> gsResp = api.getGameSystems().execute();
                if (gsResp.isSuccessful() && gsResp.body() != null) {
                    List<GameSystemEntity> gs = gsResp.body().results.stream()
                            .map(GameSystemMapper::dtoToEntity)
                            .collect(Collectors.toList());
                    gameSystemDao.insertAll(gs);
                }

                // licenses
                Response<LicenseResponse> licResp = api.getLicenses().execute();
                if (licResp.isSuccessful() && licResp.body() != null) {
                    List<LicenseEntity> lic = licResp.body().results.stream()
                            .map(LicenseMapper::dtoToEntity)
                            .collect(Collectors.toList());
                    licenseDao.insertAll(lic);
                }

                // documents
                Response<DocumentResponse> docResp = api.getDocuments().execute();
                if (docResp.isSuccessful() && docResp.body() != null) {
                    List<DocumentEntity> docs = docResp.body().results.stream()
                            .map(DocumentMapper::dtoToEntity)
                            .collect(Collectors.toList());
                    documentDao.insertAll(docs);
                }

                // languages
                Response<LanguageResponse> langResp = api.getLanguages().execute();
                if (langResp.isSuccessful() && langResp.body() != null) {
                    List<LanguageEntity> lungs = langResp.body().results.stream()
                                    .map(LanguageMapper::dtoToEntity)
                                    .collect(Collectors.toList());
                    languageDao.insertAll(lungs);
                }
            } catch (IOException e) {
                Log.e("Open5eRepository", "Error refreshing data", e);
            }
    }
}
