package com.fizzycoyote.qusetroll.feature_loading;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.fizzycoyote.qusetroll.R;
import com.fizzycoyote.qusetroll.core.api.Open5eApiClient;
import com.fizzycoyote.qusetroll.core.local_database.Open5eDatabase;
import com.fizzycoyote.qusetroll.core.repository.open5e.Open5eRepository;
import com.fizzycoyote.qusetroll.main.ui.MainActivity;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class LoadingActivity extends AppCompatActivity {
    private Open5eRepository repository;
    private ProgressBar progressBar;
    private TextView progressText;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);

        progressBar = findViewById(R.id.progressBar);
        progressText = findViewById(R.id.progressText);

        Open5eDatabase db = Open5eDatabase.getInstance(getApplicationContext());
        Executor executor = Executors.newSingleThreadExecutor();

        repository = new Open5eRepository(
                Open5eApiClient.getApiService(),
                db.publisherDao(),
                db.gameSystemDao(),
                db.licenseDao(),
                db.documentDao(),
                db.languageDao(),
                db.abilityDao(),
                db.skillDao(),
                db.characterClassDao(),
                db.featureDao(),
                db.hitPointsDao(),
                db.savingThrowDao(),
                db.spellDao(),
                db.spellSchoolDao(),
                db.creatureDao(),
                db.speciesDao(),
                executor
        );

        boolean forceRefresh = getIntent().getBooleanExtra("force_refresh", false);
        if (forceRefresh) {
            startFetching();
        } else {
            checkDataAndProceed();
        }
    }

    private void checkDataAndProceed() {
        Open5eDatabase.getInstance(this).getQueryExecutor().execute(() -> {
            int classCount = Open5eDatabase.getInstance(this)
                    .characterClassDao().getCount();
            int spellCount = Open5eDatabase.getInstance(this)
                    .spellDao().getCount();

            runOnUiThread(() -> {
                if (classCount > 0 && spellCount > 0) {
                    startMainActivity();
                } else {
                    startFetching();
                }
            });
        });
    }

    private void startFetching() {
        repository.refreshAllData().observe(this, resource -> {
            if (resource == null) return;

            switch (resource.status) {
                case LOADING:
                    updateUI(resource.progress);
                    break;
                case SUCCESS:
                    startMainActivity();
                    break;
                case ERROR:
                    showError(resource.message);
                    break;
            }
        });
    }

    private void updateUI(int progress) {
        runOnUiThread(() -> {
            progressBar.setProgress(progress);
            progressText.setText("Loading... " + progress + "%");
        });
    }

    private void startMainActivity() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    private void showError(String message) {
        new AlertDialog.Builder(this)
                .setTitle("Error")
                .setMessage(message)
                .setPositiveButton("Retry", (d, w) -> startFetching())
                .setNegativeButton("Exit", (d, w) -> finish())
                .show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        repository.refreshAllData().removeObservers(this);
    }
}