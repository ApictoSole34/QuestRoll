package com.fizzycoyote.qusetroll.feature_loading;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;

import com.fizzycoyote.qusetroll.R;
import com.fizzycoyote.qusetroll.core.api.Open5eApiClient;
import com.fizzycoyote.qusetroll.core.local_database.Open5eDatabase;
import com.fizzycoyote.qusetroll.core.repository.open5e.Open5eRepository;
import com.fizzycoyote.qusetroll.core.repository.open5e.Resource;
import com.fizzycoyote.qusetroll.main.ui.MainActivity;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class LoadingActivity extends AppCompatActivity {

    private Open5eRepository repository;

    private ScrollView selectionLayout;
    private LinearLayout fetchingLayout;
    private ProgressBar progressBar;
    private ProgressBar progressBarIndeterminate;
    private TextView progressText;
    private TextView sectionText;
    private TextView logText;

    private final Map<DataSection, CheckBox> checkboxMap = new LinkedHashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);

        selectionLayout = findViewById(R.id.layout_selection);
        fetchingLayout = findViewById(R.id.layout_fetching);
        progressBar = findViewById(R.id.progressBar);
        progressBarIndeterminate = findViewById(R.id.progressBarIndeterminate);
        progressText = findViewById(R.id.progressText);
        sectionText = findViewById(R.id.sectionText);
        logText = findViewById(R.id.logText);

        buildRepository();
        setupCheckboxes();
        setupButtons();

        boolean forceRefresh = getIntent().getBooleanExtra("force_refresh", false);
        if (forceRefresh) {
            showSelectionLayout();
        } else {
            checkDataAndProceed();
        }
    }
    private void buildRepository() {
        Open5eDatabase db = Open5eDatabase.getInstance(getApplicationContext());
        Executor executor = Executors.newSingleThreadExecutor();
        repository = new Open5eRepository(
                Open5eApiClient.getApiService(),
                db.publisherDao(), db.gameSystemDao(), db.licenseDao(),
                db.documentDao(), db.languageDao(), db.abilityDao(),
                db.skillDao(), db.characterClassDao(), db.featureDao(),
                db.hitPointsDao(), db.savingThrowDao(), db.spellDao(),
                db.spellSchoolDao(), db.creatureDao(), db.speciesDao(),
                db.backgroundDao(), db.itemDao(), db.damageTypeDao(),
                db.alignmentDao(), db.itemRarityDao(), db.weaponPropertyDao(),
                db.serviceDao(), db.environmentDao(),
                executor
        );
    }

    private void setupCheckboxes() {
        LinearLayout checkboxContainer = findViewById(R.id.checkbox_container);
        for (DataSection section : DataSection.values()) {
            CheckBox cb = new CheckBox(this);
            cb.setText(section.displayName);
            cb.setChecked(true);
            cb.setPadding(0, dp(6), 0, dp(6));
            checkboxMap.put(section, cb);
            checkboxContainer.addView(cb);
        }
    }

    private void setupButtons() {
        findViewById(R.id.btnFetchAll).setOnClickListener(v -> {
            for (CheckBox cb : checkboxMap.values()) cb.setChecked(true);
            showFetchingLayout();
            startFetchingAll();
        });

        findViewById(R.id.btnFetchSelected).setOnClickListener(v -> {
            Set<DataSection> selected = getSelectedSections();
            if (selected.isEmpty()) {
                Toast.makeText(this, "Select at least one section", Toast.LENGTH_SHORT).show();
                return;
            }
            showFetchingLayout();
            startFetchingSelected(selected);
        });

        findViewById(R.id.btnSkip).setOnClickListener(v -> startMainActivity());

        findViewById(R.id.btnSelectAll).setOnClickListener(v ->
                checkboxMap.values().forEach(cb -> cb.setChecked(true)));

        findViewById(R.id.btnSelectNone).setOnClickListener(v ->
                checkboxMap.values().forEach(cb -> cb.setChecked(false)));
    }

    private Set<DataSection> getSelectedSections() {
        Set<DataSection> selected = new LinkedHashSet<>();
        for (Map.Entry<DataSection, CheckBox> entry : checkboxMap.entrySet()) {
            if (entry.getValue().isChecked()) selected.add(entry.getKey());
        }
        return selected;
    }

    private void checkDataAndProceed() {
        Open5eDatabase.getInstance(this).getQueryExecutor().execute(() -> {
            int classCount = Open5eDatabase.getInstance(this).characterClassDao().getCount();
            int spellCount = Open5eDatabase.getInstance(this).spellDao().getCount();
            runOnUiThread(() -> {
                if (classCount > 0 && spellCount > 0) {
                    startMainActivity();
                } else {
                    showSelectionLayout();
                }
            });
        });
    }

    private void showSelectionLayout() {
        selectionLayout.setVisibility(View.VISIBLE);
        fetchingLayout.setVisibility(View.GONE);
    }

    private void showFetchingLayout() {
        selectionLayout.setVisibility(View.GONE);
        fetchingLayout.setVisibility(View.VISIBLE);
        logText.setText("");
    }

    private void startFetchingAll() {
        observeFetching(repository.refreshAllData());
    }

    private void startFetchingSelected(Set<DataSection> sections) {
        observeFetching(repository.refreshSelectedData(sections));
    }

    private void observeFetching(LiveData<Resource<Boolean>> liveData) {
        liveData.observe(this, resource -> {
            if (resource == null) return;
            switch (resource.status) {
                case LOADING:
                    updateUI(resource.progress, resource.sectionName);
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

    private void updateUI(int progress, String sectionName) {
        runOnUiThread(() -> {
            progressBar.setProgress(progress);
            progressText.setText(progress + "%");

            if (sectionName != null && !sectionName.isEmpty()) {
                sectionText.setText("Loading: " + sectionName);
                String current = logText.getText().toString();
                String newLog = current.isEmpty()
                        ? "✅ " + sectionName
                        : current + "\n✅ " + sectionName;
                logText.setText(newLog);
            }
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
                .setPositiveButton("Retry", (d, w) -> {
                    showFetchingLayout();
                    startFetchingAll();
                })
                .setNegativeButton("Exit", (d, w) -> finish())
                .show();
    }

    private int dp(int v) { return (int)(v * getResources().getDisplayMetrics().density); }
}