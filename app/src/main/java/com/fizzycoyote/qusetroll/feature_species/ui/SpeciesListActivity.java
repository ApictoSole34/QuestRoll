package com.fizzycoyote.qusetroll.feature_species.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fizzycoyote.qusetroll.R;
import com.fizzycoyote.qusetroll.core.local_database.Open5eDatabase;
import com.fizzycoyote.qusetroll.core.local_database.UserContentDatabase;
import com.fizzycoyote.qusetroll.feature_species.adapter.SpeciesAdapter;
import com.fizzycoyote.qusetroll.feature_species.model.SpeciesFilter;
import com.fizzycoyote.qusetroll.feature_species.viewmodel.SpeciesListViewModel;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.List;

public class SpeciesListActivity extends AppCompatActivity {

    private SpeciesListViewModel viewModel;
    private SpeciesAdapter adapter;
    private RecyclerView rv;
    private ChipGroup chipGroupSources;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_species_list);

        Open5eDatabase open5eDb = Open5eDatabase.getInstance(this);
        UserContentDatabase customDb = UserContentDatabase.getInstance(this);

        viewModel = new ViewModelProvider(this,
                new SpeciesListViewModel.Factory(
                        open5eDb.speciesDao(),
                        customDb.customSpeciesDao()
                )).get(SpeciesListViewModel.class);

        chipGroupSources = findViewById(R.id.chip_group_sources);
        rv = findViewById(R.id.recycler_species);

        setupRecyclerView();
        setupSearch();
        setupObservers();
        viewModel.loadSources();

        findViewById(R.id.btnFilter).setOnClickListener(v -> showFilterDialog());
        findViewById(R.id.fabCreateSpecies).setOnClickListener(v ->
                startActivity(new Intent(this, CustomSpeciesCreateActivity.class)));
    }

    private void setupRecyclerView() {
        adapter = new SpeciesAdapter(species -> {
            if (species.isCustom) {
                Intent i = new Intent(this, CustomSpeciesDetailActivity.class);
                i.putExtra("CUSTOM_SPECIES_ID", species.customId);
                startActivity(i);
            } else {
                Intent i = new Intent(this, SpeciesDetailActivity.class);
                i.putExtra("SPECIES_KEY", species.key);
                startActivity(i);
            }
        });
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adapter);
    }

    private void setupSearch() {
        ((SearchView) findViewById(R.id.search_view))
                .setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                    @Override public boolean onQueryTextSubmit(String q) { viewModel.setQuery(q); return true; }
                    @Override public boolean onQueryTextChange(String q) { viewModel.setQuery(q); return true; }
                });
    }

    private void setupObservers() {
        viewModel.getSpecies().observe(this, species -> {
            adapter.submitList(species, () -> rv.scrollToPosition(0));
            ((TextView) findViewById(R.id.tv_species_count))
                    .setText(species.size() + " species");
        });

        viewModel.getFilter().observe(this, f -> {
            TextView tv = findViewById(R.id.tv_active_filters);
            if (f.isEmpty()) { tv.setVisibility(View.GONE); }
            else { tv.setVisibility(View.VISIBLE); tv.setText(buildFilterSummary(f)); }
        });

        viewModel.getSources().observe(this, sources -> {
            if (sources == null || sources.isEmpty()) return;
            buildSourceChips(sources);
        });
    }

    private void buildSourceChips(List<String> sources) {
        int count = chipGroupSources.getChildCount();
        if (count > 1) chipGroupSources.removeViews(1, count - 1);

        for (String source : sources) {
            Chip chip = new Chip(this);
            chip.setText(formatSource(source));
            chip.setTag(source);
            chip.setCheckable(true);
            chip.setChipBackgroundColorResource(android.R.color.transparent);
            chip.setCheckedIconVisible(true);
            chip.setOnCheckedChangeListener((btn, isChecked) -> {
                if (isChecked) {
                    Chip chipAll = findViewById(R.id.chip_all);
                    if (chipAll != null) chipAll.setChecked(false);
                    viewModel.setSource((String) btn.getTag());
                }
            });
            chipGroupSources.addView(chip);
        }

        Chip chipAll = findViewById(R.id.chip_all);
        if (chipAll != null) {
            chipAll.setOnCheckedChangeListener((btn, isChecked) -> {
                if (isChecked) {
                    for (int i = 1; i < chipGroupSources.getChildCount(); i++)
                        ((Chip) chipGroupSources.getChildAt(i)).setChecked(false);
                    viewModel.setSource("");
                }
            });
        }
    }

    private String formatSource(String source) {
        if (source == null) return "Unknown";
        if (source.equals("custom")) return "Custom";
        if (source.contains("System Reference Document 5.1")) return "SRD 5.1";
        if (source.contains("System Reference Document 5.2")) return "SRD 5.2";
        if (source.contains("Open5e")) return "Open5e";
        if (source.contains("Level Up")) return "Level Up";
        return source.length() > 12 ? source.substring(0, 12) + "…" : source;
    }

    private String buildFilterSummary(SpeciesFilter f) {
        List<String> parts = new ArrayList<>();
        if (f.subspeciesOnly) parts.add("Subspecies only");
        if (f.mainOnly) parts.add("Main only");
        if (!f.source.isEmpty()) parts.add(f.source);
        if (!f.query.isEmpty()) parts.add("\"" + f.query + "\"");
        return "Filters: " + String.join(", ", parts);
    }

    private void showFilterDialog() {
        View dv = LayoutInflater.from(this).inflate(R.layout.dialog_species_filter, null);
        CheckBox cbSubspecies = dv.findViewById(R.id.cb_subspecies_only);
        CheckBox cbMainOnly = dv.findViewById(R.id.cb_main_only);

        SpeciesFilter current = viewModel.getFilter().getValue();
        if (current == null) current = new SpeciesFilter();
        cbSubspecies.setChecked(current.subspeciesOnly);
        cbMainOnly.setChecked(current.mainOnly);

        cbSubspecies.setOnCheckedChangeListener((v, checked) -> {
            if (checked) cbMainOnly.setChecked(false);
        });
        cbMainOnly.setOnCheckedChangeListener((v, checked) -> {
            if (checked) cbSubspecies.setChecked(false);
        });

        new AlertDialog.Builder(this)
                .setTitle("Filter Species")
                .setView(dv)
                .setPositiveButton("Apply", (d, w) ->
                        viewModel.applySubspeciesFilter(
                                cbSubspecies.isChecked(),
                                cbMainOnly.isChecked()))
                .setNeutralButton("Clear", (d, w) -> viewModel.clearFilters())
                .setNegativeButton("Cancel", null)
                .show();
    }
}