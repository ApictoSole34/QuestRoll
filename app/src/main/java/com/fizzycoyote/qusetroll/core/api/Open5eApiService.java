package com.fizzycoyote.qusetroll.core.api;

import com.fizzycoyote.qusetroll.core.models.open5e.document.DocumentResponse;
import com.fizzycoyote.qusetroll.core.models.open5e.game_system.GameSystemResponse;
import com.fizzycoyote.qusetroll.core.models.open5e.language.LanguageResponse;
import com.fizzycoyote.qusetroll.core.models.open5e.license.LicenseResponse;
import com.fizzycoyote.qusetroll.core.models.open5e.publisher.PublisherResponse;

import retrofit2.Call;
import retrofit2.http.GET;

public interface Open5eApiService {
    @GET("documents/")
    Call<DocumentResponse> getDocuments();

    @GET("gamesystems/")
    Call<GameSystemResponse> getGameSystems();

    @GET("licenses/")
    Call<LicenseResponse> getLicenses();

    @GET("languages/")
    Call<LanguageResponse> getLanguages();

    @GET("publishers/")
    Call<PublisherResponse> getPublishers();
}