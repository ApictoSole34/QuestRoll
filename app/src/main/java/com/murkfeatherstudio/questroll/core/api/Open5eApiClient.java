package com.murkfeatherstudio.questroll.core.api;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Client for the Open5e API.
 * <p>
 * This class provides a singleton {@link Open5eApiService} instance configured with
 * custom timeouts and a Gson converter. The HTTP client is configured to handle
 * potentially slow responses from the game data API.
 * </p>
 */
public class Open5eApiClient {
    private static final String BASE_URL = "https://api.open5e.com/v2/";
    private static Retrofit retrofit = null;

    /**
     * Provides the singleton API service instance.
     * <p>
     * If the {@link Retrofit} instance hasn't been created yet, it initializes it
     * with an {@link OkHttpClient} that has 60-second timeouts for connect, read,
     * and write operations.
     * </p>
     *
     * @return The {@link Open5eApiService} used for making network requests.
     */
    public static Open5eApiService getApiService() {
        if (retrofit == null) {
            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(60, TimeUnit.SECONDS)
                    .readTimeout(60, TimeUnit.SECONDS)
                    .writeTimeout(60, TimeUnit.SECONDS)
                    .retryOnConnectionFailure(true)
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit.create(Open5eApiService.class);
    }
}
