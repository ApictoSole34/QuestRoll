package com.murkfeatherstudio.questroll.feature_loading;

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
import androidx.core.content.res.ResourcesCompat;
import androidx.lifecycle.LiveData;

import com.murkfeatherstudio.questroll.R;
import com.murkfeatherstudio.questroll.core.api.Open5eApiClient;
import com.murkfeatherstudio.questroll.core.base.BaseActivity;
import com.murkfeatherstudio.questroll.core.local_database.Open5eDatabase;
import com.murkfeatherstudio.questroll.core.repository.open5e.Open5eRepository;
import com.murkfeatherstudio.questroll.core.repository.open5e.Resource;
import com.murkfeatherstudio.questroll.main.ui.MainActivity;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * The initial activity responsible for ensuring the local database is populated
 * with game data from the Open5e API.
 * <p>
 * If data is missing, it allows the user to select specific sections (classes, spells, etc.)
 * to fetch. It displays progress logs and bars during the synchronization process.
 * Once data is available, it navigates to the {@link MainActivity}.
 * </p>
 */
public class LoadingActivity extends BaseActivity {

    private Open5eRepository repository;

    private ScrollView selectionLayout;
    private LinearLayout fetchingLayout;

    private ProgressBar progressBar;
    private ProgressBar progressBarSection;

    private TextView progressText;
    private TextView sectionText;
    private TextView logText;

    private final Map<DataSection, CheckBox> checkboxMap = new LinkedHashMap<>();
    private boolean isInternalChange = false;
    private String lastLoggedSection = "";
    private int currentOverallProgress = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);

        selectionLayout      = findViewById(R.id.layout_selection);
        fetchingLayout       = findViewById(R.id.layout_fetching);
        progressBar          = findViewById(R.id.progressBar);
        progressBarSection   = findViewById(R.id.progressBarIndeterminate);
        progressText         = findViewById(R.id.progressText);
        sectionText          = findViewById(R.id.sectionText);
        logText              = findViewById(R.id.logText);

        progressText.setTypeface(ResourcesCompat.getFont(this, R.font.inter_regular));
        progressText.setTextColor(getResources().getColor(R.color.threads_text_primary, null));

        sectionText.setTypeface(ResourcesCompat.getFont(this, R.font.inter_regular));
        sectionText.setTextColor(getResources().getColor(R.color.threads_text_primary, null));

        logText.setTypeface(ResourcesCompat.getFont(this, R.font.inter_regular));
        logText.setTextColor(getResources().getColor(R.color.threads_text_primary, null));

        progressBarSection.setIndeterminate(false);
        progressBarSection.setMax(100);
        progressBarSection.setProgress(0);

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
                db.serviceDao(), db.environmentDao(), db.ruleDao(), db.rulesetDao(),
                db.conditionDao(), db.creatureTypeDao(), db.itemCategoryDao(),
                db.itemSetDao(),
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

            cb.setTypeface(ResourcesCompat.getFont(this, R.font.inter_regular));
            cb.setTextColor(getResources().getColor(R.color.threads_text_primary, null));

            checkboxMap.put(section, cb);
            checkboxContainer.addView(cb);

            cb.setOnCheckedChangeListener((buttonView, isChecked) -> {
                handleDependencyChange(section, isChecked);
            });
        }
    }

    private void setupButtons() {
        findViewById(R.id.btnFetchAll).setOnClickListener(v -> {
            isInternalChange = true;
            for (CheckBox cb : checkboxMap.values()) cb.setChecked(true);
            isInternalChange = false;
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

        findViewById(R.id.btnSelectAll).setOnClickListener(v -> {
            isInternalChange = true;
            checkboxMap.values().forEach(cb -> cb.setChecked(true));
            isInternalChange = false;
        });

        findViewById(R.id.btnSelectNone).setOnClickListener(v -> {
            isInternalChange = true;
            checkboxMap.values().forEach(cb -> cb.setChecked(false));
            isInternalChange = false;
        });
    }

    private void handleDependencyChange(DataSection section, boolean isChecked) {
        if (isInternalChange) return;
        isInternalChange = true;
        if (isChecked) {
            autoSelectDependencies(section);
        } else {
            autoUnselectDependents(section);
        }
        isInternalChange = false;
    }

    private void autoSelectDependencies(DataSection section) {
        for (DataSection dep : getDependencies(section)) {
            CheckBox cb = checkboxMap.get(dep);
            if (cb != null && !cb.isChecked()) {
                cb.setChecked(true);
                autoSelectDependencies(dep);
            }
        }
    }

    private void autoUnselectDependents(DataSection section) {
        for (DataSection other : DataSection.values()) {
            if (getDependencies(other).contains(section)) {
                CheckBox cb = checkboxMap.get(other);
                if (cb != null && cb.isChecked()) {
                    cb.setChecked(false);
                    autoUnselectDependents(other);
                }
            }
        }
    }

    private List<DataSection> getDependencies(DataSection section) {
        List<DataSection> deps = new ArrayList<>();
        // Most content depends on basic infrastructure
        if (section != DataSection.PUBLISHERS && section != DataSection.LICENSES &&
                section != DataSection.GAME_SYSTEMS && section != DataSection.DOCUMENTS) {
            deps.add(DataSection.DOCUMENTS);
        }

        switch (section) {
            case DOCUMENTS:
                deps.add(DataSection.PUBLISHERS);
                deps.add(DataSection.LICENSES);
                deps.add(DataSection.GAME_SYSTEMS);
                break;
            case CLASSES:
                deps.add(DataSection.GAME_SYSTEMS);
                break;
            case SPELLS:
                deps.add(DataSection.SPELL_SCHOOLS);
                break;
            case CREATURES:
                deps.add(DataSection.CREATURE_TYPES);
                deps.add(DataSection.ALIGNMENTS);
                break;
            case ITEMS:
                deps.add(DataSection.ITEM_CATEGORIES);
                deps.add(DataSection.ITEM_RARITIES);
                deps.add(DataSection.GAME_SYSTEMS);
                break;
            case RULES:
                deps.add(DataSection.RULESETS);
                break;
            case ITEM_SETS:
                deps.add(DataSection.ITEMS);
                break;
        }
        return deps;
    }

    private Set<DataSection> getSelectedSections() {
        Set<DataSection> selected = new LinkedHashSet<>();
        for (Map.Entry<DataSection, CheckBox> entry : checkboxMap.entrySet()) {
            if (entry.getValue().isChecked()) selected.add(entry.getKey());
        }
        return selected;
    }

    /**
     * Checks if the local database already contains essential game data.
     * If yes, proceeds to MainActivity; otherwise, shows the selection UI for fetching.
     */
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
        lastLoggedSection = "";
        currentOverallProgress = 0;

        progressBar.setProgress(0);
        progressText.setText("0%");
        sectionText.setText("Initializing...");

        progressBar.setVisibility(View.VISIBLE);

        progressBarSection.setProgress(0);
        progressBarSection.setVisibility(View.VISIBLE);
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
                    updateUI(resource.progress, resource.sectionName, resource.sectionProgress);
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

    /**
     * Updates the progress bars and logs based on the current synchronization state.
     *
     * @param progress        Overall progress percentage (0-100).
     * @param sectionName     Name of the section currently being fetched.
     * @param sectionProgress Progress within the current section.
     */
    private void updateUI(int progress, String sectionName, int sectionProgress) {
        runOnUiThread(() -> {
            if (progress >= 0) {
                currentOverallProgress = progress;
                progressBar.setProgress(progress);
                progressText.setText(progress + "%");
            }

            if (sectionName != null && !sectionName.isEmpty()) {
                String sectionLabel = "Downloading: " + sectionName;
                if (sectionProgress >= 0) {
                    sectionLabel += "  (" + sectionProgress + "%)";
                }
                sectionText.setText(sectionLabel);

                if (sectionProgress >= 0) {
                    progressBarSection.setProgress(sectionProgress);
                } else {
                    progressBarSection.setProgress(0);
                }

                if (!sectionName.equals(lastLoggedSection)) {
                    String current = logText.getText().toString();
                    String newLog  = current.isEmpty()
                            ? "▶ " + sectionName
                            : current + "\n▶ " + sectionName;
                    logText.setText(newLog);
                    lastLoggedSection = sectionName;
                    final View scrollView = (View) logText.getParent().getParent();
                    if (scrollView instanceof androidx.core.widget.NestedScrollView) {
                        ((androidx.core.widget.NestedScrollView) scrollView).fullScroll(View.FOCUS_DOWN);
                    }
                }
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

    private int dp(int v) {
        return (int) (v * getResources().getDisplayMetrics().density);
    }
}
