package com.murkfeatherstudio.questroll.feature_loading;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.core.content.res.ResourcesCompat;
import androidx.core.widget.NestedScrollView;
import androidx.lifecycle.ViewModelProvider;

import com.murkfeatherstudio.questroll.R;
import com.murkfeatherstudio.questroll.core.base.BaseActivity;
import com.murkfeatherstudio.questroll.databinding.ActivityLoadingBinding;
import com.murkfeatherstudio.questroll.main.ui.MainActivity;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * The initial activity responsible for ensuring the local database is populated
 * with game data from the Open5e API.
 * <p>
 * All fetching state lives in {@link LoadingViewModel}, so it survives configuration
 * changes (e.g. rotation) instead of restarting the download.
 * </p>
 */
public class LoadingActivity extends BaseActivity {

    private ActivityLoadingBinding binding;
    private LoadingViewModel viewModel;

    private final Map<DataSection, CheckBox> checkboxMap = new LinkedHashMap<>();
    private boolean isInternalChange = false;
    private String lastLoggedSection = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Keep the screen on during the initial loading process
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        binding = ActivityLoadingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(LoadingViewModel.class);

        binding.progressText.setTypeface(ResourcesCompat.getFont(this, R.font.inter_regular));
        binding.progressText.setTextColor(getResources().getColor(R.color.threads_text_primary, null));

        binding.sectionText.setTypeface(ResourcesCompat.getFont(this, R.font.inter_regular));
        binding.sectionText.setTextColor(getResources().getColor(R.color.threads_text_primary, null));

        binding.logText.setTypeface(ResourcesCompat.getFont(this, R.font.inter_regular));
        binding.logText.setTextColor(getResources().getColor(R.color.threads_text_primary, null));

        binding.progressBarIndeterminate.setIndeterminate(false);
        binding.progressBarIndeterminate.setMax(100);
        binding.progressBarIndeterminate.setProgress(0);

        setupCheckboxes();
        setupButtons();
        observeViewModel();

        boolean forceRefresh = getIntent().getBooleanExtra("force_refresh", false);
        if (forceRefresh) {
            showSelectionLayout();
        } else if (viewModel.isFetchStarted()) {
            // A fetch was already running before rotation - just re-show the UI for it.
            showFetchingLayout();
        } else {
            viewModel.checkExistingData();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }

    private void observeViewModel() {
        viewModel.getHasExistingData().observe(this, hasData -> {
            if (hasData == null) return;
            if (hasData) {
                startMainActivity();
            } else {
                showSelectionLayout();
            }
        });

        viewModel.getFetchState().observe(this, resource -> {
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

    private void setupCheckboxes() {
        LinearLayout checkboxContainer = binding.checkboxContainer;
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
        binding.btnFetchAll.setOnClickListener(v -> {
            isInternalChange = true;
            for (CheckBox cb : checkboxMap.values()) cb.setChecked(true);
            isInternalChange = false;
            showFetchingLayout();
            viewModel.startFetchingAll();
        });

        binding.btnFetchSelected.setOnClickListener(v -> {
            Set<DataSection> selected = getSelectedSections();
            if (selected.isEmpty()) {
                Toast.makeText(this, "Select at least one section", Toast.LENGTH_SHORT).show();
                return;
            }
            showFetchingLayout();
            viewModel.startFetchingSelected(selected);
        });

        binding.btnSkip.setOnClickListener(v -> startMainActivity());

        binding.btnSelectAll.setOnClickListener(v -> {
            isInternalChange = true;
            checkboxMap.values().forEach(cb -> cb.setChecked(true));
            isInternalChange = false;
        });

        binding.btnSelectNone.setOnClickListener(v -> {
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

    private void showSelectionLayout() {
        binding.layoutSelection.setVisibility(View.VISIBLE);
        binding.layoutFetching.setVisibility(View.GONE);
    }

    private void showFetchingLayout() {
        binding.layoutSelection.setVisibility(View.GONE);
        binding.layoutFetching.setVisibility(View.VISIBLE);
        binding.logText.setText("");
        lastLoggedSection = "";

        binding.progressBar.setProgress(0);
        binding.progressText.setText("0%");
        binding.sectionText.setText("Initializing...");

        binding.progressBar.setVisibility(View.VISIBLE);

        binding.progressBarIndeterminate.setProgress(0);
        binding.progressBarIndeterminate.setVisibility(View.VISIBLE);
    }

    private void updateUI(int progress, String sectionName, int sectionProgress) {
        if (progress >= 0) {
            binding.progressBar.setProgress(progress);
            binding.progressText.setText(progress + "%");
        }

        if (sectionName != null && !sectionName.isEmpty()) {
            String sectionLabel = "Downloading: " + sectionName;
            if (sectionProgress >= 0) {
                sectionLabel += "  (" + sectionProgress + "%)";
            }
            binding.sectionText.setText(sectionLabel);

            binding.progressBarIndeterminate.setProgress(Math.max(sectionProgress, 0));

            if (!sectionName.equals(lastLoggedSection)) {
                String current = binding.logText.getText().toString();
                String newLog  = current.isEmpty()
                        ? "▶ " + sectionName
                        : current + "\n▶ " + sectionName;
                binding.logText.setText(newLog);
                lastLoggedSection = sectionName;

                // Note: the parent ScrollView has no id in the layout, so it isn't part of
                // the binding - we still have to walk up to reach it for the auto-scroll.
                View parent = (View) binding.logText.getParent().getParent();
                if (parent instanceof NestedScrollView) {
                    ((NestedScrollView) parent).fullScroll(View.FOCUS_DOWN);
                } else if (parent instanceof android.widget.ScrollView) {
                    ((android.widget.ScrollView) parent).fullScroll(View.FOCUS_DOWN);
                }
            }
        }
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
                    viewModel.retryFetchAll();
                })
                .setNegativeButton("Exit", (d, w) -> finish())
                .show();
    }

    private int dp(int v) {
        return (int) (v * getResources().getDisplayMetrics().density);
    }
}
