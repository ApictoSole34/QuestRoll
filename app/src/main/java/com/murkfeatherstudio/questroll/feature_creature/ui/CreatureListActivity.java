package com.murkfeatherstudio.questroll.feature_creature.ui;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;

import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.murkfeatherstudio.questroll.R;
import com.murkfeatherstudio.questroll.core.base.BaseActivity;
import com.murkfeatherstudio.questroll.core.local_database.Open5eDatabase;
import com.murkfeatherstudio.questroll.core.local_database.UserContentDatabase;
import com.murkfeatherstudio.questroll.databinding.ActivityCreatureListBinding;
import com.murkfeatherstudio.questroll.databinding.DialogCreatureFilterBinding;
import com.murkfeatherstudio.questroll.feature_creature.adapter.CreatureAdapter;
import com.murkfeatherstudio.questroll.feature_creature.model.CreatureFilter;
import com.murkfeatherstudio.questroll.feature_creature.viewmodel.CreatureListViewModel;
import com.google.android.material.chip.Chip;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CreatureListActivity extends BaseActivity {

    private CreatureListViewModel viewModel;
    private CreatureAdapter adapter;
    private ActivityCreatureListBinding binding;

    private List<String> currentTypeNames = new ArrayList<>(Collections.singletonList(""));

    private static final String[] ALIGNMENTS = {
            "", "lawful good", "neutral good", "chaotic good",
            "lawful neutral", "neutral", "chaotic neutral",
            "lawful evil", "neutral evil", "chaotic evil",
            "unaligned", "any alignment"
    };
    private static final String[] ALIGNMENT_LABELS = {
            "All", "Lawful Good", "Neutral Good", "Chaotic Good",
            "Lawful Neutral", "Neutral", "Chaotic Neutral",
            "Lawful Evil", "Neutral Evil", "Chaotic Evil",
            "Unaligned", "Any"
    };

    private static final float[] CR_VALUES = {
            0f, 0.125f, 0.25f, 0.5f, 1f, 2f, 3f, 4f, 5f, 6f, 7f, 8f, 9f, 10f,
            11f, 12f, 13f, 14f, 15f, 16f, 17f, 18f, 19f, 20f, 21f, 22f, 23f, 24f, 30f
    };
    private static final String[] CR_LABELS = {
            "0", "1/8", "1/4", "1/2", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10",
            "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "30"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCreatureListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Open5eDatabase open5eDb = Open5eDatabase.getInstance(this);
        UserContentDatabase customDb = UserContentDatabase.getInstance(this);

        viewModel = new ViewModelProvider(this,
                new CreatureListViewModel.Factory(
                        open5eDb.creatureDao(),
                        customDb.customCreatureDao(),
                        customDb.customCreatureTypeDao(),
                        open5eDb.creatureTypeDao()
                )).get(CreatureListViewModel.class);

        setupRecyclerView();
        setupSearch();
        setupObservers();
        viewModel.loadSources();

        binding.btnFilter.setOnClickListener(v -> showFilterDialog());
        binding.fabCreateCreature.setOnClickListener(v ->
                startActivity(new Intent(this, CustomCreatureCreateActivity.class)));
        binding.btnManageTypes.setOnClickListener(v ->
                new CustomCreatureTypeBottomSheet()
                        .show(getSupportFragmentManager(), "creature_types"));
    }

    private void setupRecyclerView() {
        adapter = new CreatureAdapter(creature -> {
            if (creature.isCustom) {
                Intent i = new Intent(this, CustomCreatureDetailActivity.class);
                i.putExtra("CUSTOM_CREATURE_ID", creature.customId);
                startActivity(i);
            } else {
                Intent i = new Intent(this, CreatureDetailActivity.class);
                i.putExtra("CREATURE_KEY", creature.key);
                startActivity(i);
            }
        });
        binding.recyclerCreatures.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerCreatures.setAdapter(adapter);
    }

    private void setupSearch() {
        binding.searchView.setOnQueryTextListener(new android.widget.SearchView.OnQueryTextListener() {
            @Override public boolean onQueryTextSubmit(String q) { viewModel.setQuery(q); return true; }
            @Override public boolean onQueryTextChange(String q) { viewModel.setQuery(q); return true; }
        });
    }

    private void setupObservers() {
        viewModel.getCreatures().observe(this, creatures -> {
            adapter.submitList(creatures, () -> binding.recyclerCreatures.scrollToPosition(0));
            binding.tvCreatureCount.setText(creatures.size() + " creatures");
        });

        viewModel.getFilter().observe(this, f -> {
            if (f.isEmpty()) { 
                binding.tvActiveFilters.setVisibility(View.GONE); 
            } else { 
                binding.tvActiveFilters.setVisibility(View.VISIBLE); 
                binding.tvActiveFilters.setText(buildFilterSummary(f)); 
            }
        });

        viewModel.getSources().observe(this, sources -> {
            if (sources == null || sources.isEmpty()) return;
            buildSourceChips(sources);
        });

        viewModel.getAllTypeNames().observe(this, types -> {
            if (types != null) currentTypeNames = types;
        });
    }

    /**
     * JAVADOC: chipGroupSources is a dynamic container. We use removeViews() and addView() 
     * to manage Material Chips programmatically because the rule sources are loaded 
     * from the database at runtime. Static View Binding is not possible for these 
     * dynamic children.
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
        if (source.contains("Monstrous Menagerie")) return "A5e MM";
        if (source.contains("System Reference Document 5.1")) return "SRD 5.1";
        if (source.contains("System Reference Document 5.2")) return "SRD 5.2";
        if (source.contains("Tome of Beasts")) return "ToB";
        if (source.contains("Creature Codex")) return "CC";
        return source.length() > 12 ? source.substring(0, 12) + "…" : source;
    }

    private String buildFilterSummary(CreatureFilter f) {
        List<String> parts = new ArrayList<>();
        if (!f.typeKey.isEmpty()) parts.add(f.typeKey);
        if (!f.alignment.isEmpty()) parts.add(f.alignment);
        if (f.crMin >= 0) parts.add("CR ≥ " + getCrLabel(f.crMin));
        if (f.crMax >= 0) parts.add("CR ≤ " + getCrLabel(f.crMax));
        if (!f.source.isEmpty()) parts.add(f.source);
        if (!f.query.isEmpty()) parts.add("\"" + f.query + "\"");
        return "Filters: " + String.join(", ", parts);
    }

    private String getCrLabel(float cr) {
        for (int i = 0; i < CR_VALUES.length; i++)
            if (Math.abs(CR_VALUES[i] - cr) < 0.01f) return CR_LABELS[i];
        return String.valueOf((int) cr);
    }

    private void showFilterDialog() {
        DialogCreatureFilterBinding filterBinding = DialogCreatureFilterBinding.inflate(getLayoutInflater());

        CreatureFilter current = viewModel.getFilter().getValue();
        if (current == null) current = new CreatureFilter();
        final CreatureFilter finalCurrent = current;

        final List<String> finalTypeNames = currentTypeNames;

        List<String> typeLabels = new ArrayList<>();
        typeLabels.add("All Types");
        for (int i = 1; i < finalTypeNames.size(); i++) typeLabels.add(finalTypeNames.get(i));
        
        filterBinding.spinnerType.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, typeLabels));
        int ti = finalTypeNames.indexOf(finalCurrent.typeKey);
        if (ti >= 0) filterBinding.spinnerType.setSelection(ti);

        filterBinding.spinnerAlignment.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, ALIGNMENT_LABELS));
        for (int i = 0; i < ALIGNMENTS.length; i++) {
            if (ALIGNMENTS[i].equals(finalCurrent.alignment)) {
                filterBinding.spinnerAlignment.setSelection(i);
                break;
            }
        }

        List<String> crAll = new ArrayList<>(Collections.singletonList("Any"));
        crAll.addAll(Arrays.asList(CR_LABELS));
        filterBinding.spinnerCrMin.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, crAll));
        filterBinding.spinnerCrMax.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, crAll));

        new AlertDialog.Builder(this)
                .setTitle("Filter Creatures")
                .setView(filterBinding.getRoot())
                .setPositiveButton("Apply", (d, w) -> {
                    int tp = filterBinding.spinnerType.getSelectedItemPosition();
                    viewModel.setTypeKey(tp == 0 ? "" : finalTypeNames.get(tp));

                    int ap = filterBinding.spinnerAlignment.getSelectedItemPosition();
                    viewModel.setAlignment(ALIGNMENTS[ap]);

                    int crMinPos = filterBinding.spinnerCrMin.getSelectedItemPosition();
                    int crMaxPos = filterBinding.spinnerCrMax.getSelectedItemPosition();
                    viewModel.setCrRange(
                            crMinPos == 0 ? -1f : CR_VALUES[crMinPos - 1],
                            crMaxPos == 0 ? -1f : CR_VALUES[crMaxPos - 1]);
                })
                .setNeutralButton("Clear", (d, w) -> viewModel.clearFilters())
                .setNegativeButton("Cancel", null)
                .show();
    }
}
