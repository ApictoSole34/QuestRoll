package com.murkfeatherstudio.questroll.feature_species.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.SearchView;

import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.murkfeatherstudio.questroll.R;
import com.murkfeatherstudio.questroll.core.base.BaseActivity;
import com.murkfeatherstudio.questroll.core.local_database.Open5eDatabase;
import com.murkfeatherstudio.questroll.core.local_database.UserContentDatabase;
import com.murkfeatherstudio.questroll.databinding.ActivitySpeciesListBinding;
import com.murkfeatherstudio.questroll.databinding.DialogSpeciesFilterBinding;
import com.murkfeatherstudio.questroll.feature_species.adapter.SpeciesAdapter;
import com.murkfeatherstudio.questroll.feature_species.model.SpeciesFilter;
import com.murkfeatherstudio.questroll.feature_species.viewmodel.SpeciesListViewModel;
import com.google.android.material.chip.Chip;

import java.util.ArrayList;
import java.util.List;

public class SpeciesListActivity extends BaseActivity {

    private SpeciesListViewModel viewModel;
    private SpeciesAdapter adapter;
    private ActivitySpeciesListBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySpeciesListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Open5eDatabase open5eDb = Open5eDatabase.getInstance(this);
        UserContentDatabase customDb = UserContentDatabase.getInstance(this);

        viewModel = new ViewModelProvider(this,
                new SpeciesListViewModel.Factory(
                        open5eDb.speciesDao(),
                        customDb.customSpeciesDao()
                )).get(SpeciesListViewModel.class);

        setupRecyclerView();
        setupSearch();
        setupObservers();
        viewModel.loadSources();

        binding.btnFilter.setOnClickListener(v -> showFilterDialog());
        binding.fabCreateSpecies.setOnClickListener(v ->
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
        binding.recyclerSpecies.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerSpecies.setAdapter(adapter);
    }

    private void setupSearch() {
        binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override public boolean onQueryTextSubmit(String q) { viewModel.setQuery(q); return true; }
            @Override public boolean onQueryTextChange(String q) { viewModel.setQuery(q); return true; }
        });
    }

    private void setupObservers() {
        viewModel.getSpecies().observe(this, species -> {
            adapter.submitList(species, () -> binding.recyclerSpecies.scrollToPosition(0));
            binding.tvSpeciesCount.setText(species.size() + " species");
        });

        viewModel.getFilter().observe(this, f -> {
            if (f.isEmpty()) { binding.tvActiveFilters.setVisibility(View.GONE); }
            else {
                binding.tvActiveFilters.setVisibility(View.VISIBLE);
                binding.tvActiveFilters.setText(buildFilterSummary(f));
            }
        });

        viewModel.getSources().observe(this, sources -> {
            if (sources == null || sources.isEmpty()) return;
            buildSourceChips(sources);
        });
    }

    /**
     * NOTE: chip_group_sources is managed dynamically (addView()).
     * Chips for individual sources are created at runtime based on the list fetched from the ViewModel.
     * ViewBinding is not applicable to these dynamically generated elements.
     */
    private void buildSourceChips(List<String> sources) {
        int count = binding.chipGroupSources.getChildCount();
        if (count > 1) binding.chipGroupSources.removeViews(1, count - 1);

        for (String source : sources) {
            Chip chip = new Chip(this);
            chip.setText(formatSource(source));
            chip.setTag(source);
            chip.setCheckable(true);
            chip.setChipBackgroundColorResource(android.R.color.transparent);
            chip.setCheckedIconVisible(true);
            chip.setOnCheckedChangeListener((btn, isChecked) -> {
                if (isChecked) {
                    binding.chipAll.setChecked(false);
                    viewModel.setSource((String) btn.getTag());
                }
            });
            binding.chipGroupSources.addView(chip);
        }

        binding.chipAll.setOnCheckedChangeListener((btn, isChecked) -> {
            if (isChecked) {
                for (int i = 1; i < binding.chipGroupSources.getChildCount(); i++)
                    ((Chip) binding.chipGroupSources.getChildAt(i)).setChecked(false);
                viewModel.setSource("");
            }
        });
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
        DialogSpeciesFilterBinding dialogBinding = DialogSpeciesFilterBinding.inflate(getLayoutInflater());

        SpeciesFilter current = viewModel.getFilter().getValue();
        if (current == null) current = new SpeciesFilter();
        dialogBinding.cbSubspeciesOnly.setChecked(current.subspeciesOnly);
        dialogBinding.cbMainOnly.setChecked(current.mainOnly);

        dialogBinding.cbSubspeciesOnly.setOnCheckedChangeListener((v, checked) -> {
            if (checked) dialogBinding.cbMainOnly.setChecked(false);
        });
        dialogBinding.cbMainOnly.setChecked(current.mainOnly);
        dialogBinding.cbMainOnly.setOnCheckedChangeListener((v, checked) -> {
            if (checked) dialogBinding.cbSubspeciesOnly.setChecked(false);
        });

        new AlertDialog.Builder(this)
                .setTitle("Filter Species")
                .setView(dialogBinding.getRoot())
                .setPositiveButton("Apply", (d, w) ->
                        viewModel.applySubspeciesFilter(
                                dialogBinding.cbSubspeciesOnly.isChecked(),
                                dialogBinding.cbMainOnly.isChecked()))
                .setNeutralButton("Clear", (d, w) -> viewModel.clearFilters())
                .setNegativeButton("Cancel", null)
                .show();
    }
}
