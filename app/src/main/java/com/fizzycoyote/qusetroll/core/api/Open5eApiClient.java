package com.fizzycoyote.qusetroll.core.api;

import android.util.Log;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class Open5eApiClient {
    private static final String BASE_URL = "https://api.open5e.com/v2/";
    private static Retrofit retrofit = null;

    public static Open5eApiService getApiService() {
        Log.d("Open5eApiClient", "getApiService called");
        if (retrofit == null) {
            Log.d("Open5eApiClient", "retrofit == null");
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit.create(Open5eApiService.class);
    }
}
