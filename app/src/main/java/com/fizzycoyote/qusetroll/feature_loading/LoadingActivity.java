package com.fizzycoyote.qusetroll.feature_loading;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.fizzycoyote.qusetroll.R;
import com.fizzycoyote.qusetroll.core.api.Open5eApiClient;
import com.fizzycoyote.qusetroll.core.local_database.Open5eDatabase;
import com.fizzycoyote.qusetroll.core.repository.open5e.Open5eRepository;
import com.fizzycoyote.qusetroll.main.ui.MainActivity;

import java.util.concurrent.Executors;

public class LoadingActivity extends AppCompatActivity {

    private Open5eRepository repository;
    private boolean hasStartedMain = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("LoadingActivity", "onCreate");
        setContentView(R.layout.activity_loading);

        Open5eDatabase db = Open5eDatabase.getInstance(getApplicationContext());

        repository = new Open5eRepository(
                Open5eApiClient.getApiService(),
                db.publisherDao(),
                db.gameSystemDao(),
                db.licenseDao(),
                db.documentDao(),
                db.languageDao()
        );
        Log.d("LoadingActivity", "repository created");
        loadData();
        Log.d("LoadingActivity", "loadData called");
    }

    private void loadData() {
        Executors.newSingleThreadExecutor().execute(() -> {
            Log.d("LoadingActivity", "loadData executed");
            try {
                repository.refreshAllData();
                Log.d("LoadingActivity", "data refreshed");
                goToMain();
            } catch (Exception e) {
                Log.e("LoadingActivity", "Error refreshing data", e);
            }

        });
    }

    private void goToMain() {
        if (!hasStartedMain) {
            hasStartedMain = true;
            Log.d("LoadingActivity", "goToMain");
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                Intent intent = new Intent(LoadingActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }, 2000);
        }
    }

}
