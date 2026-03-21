package com.fizzycoyote.qusetroll.feature_spell.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fizzycoyote.qusetroll.R;
import com.fizzycoyote.qusetroll.core.local_database.Open5eDatabase;
import com.fizzycoyote.qusetroll.core.local_database.UserContentDatabase;
import com.fizzycoyote.qusetroll.feature_spell.view_model.SpellListViewModel;
import com.fizzycoyote.qusetroll.feature_spell.adapter.SpellAdapter;
import com.fizzycoyote.qusetroll.feature_spell.model.SpellFilter;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.List;

public class SpellListActivity extends AppCompatActivity {

    private SpellListViewModel viewModel;
    private SpellAdapter adapter;
    private TextView tvActiveFilters;
    private ChipGroup chipGroupSources;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spell_list);

        Open5eDatabase open5eDb = Open5eDatabase.getInstance(this);
        UserContentDatabase customDb = UserContentDatabase.getInstance(this);

        viewModel = new ViewModelProvider(this,
                new SpellListViewModel.Factory(
                        open5eDb.spellDao(),
                        open5eDb.spellSchoolDao(),
                        customDb.customSpellDao(),
                        customDb.customSpellSchoolDao()
                )).get(SpellListViewModel.class);

        chipGroupSources = findViewById(R.id.chip_group_sources);

        setupRecyclerView();
        setupSearch();
        setupObservers();
        viewModel.loadSchools();
        viewModel.loadSources();

        findViewById(R.id.btnFilter).setOnClickListener(v -> showFilterDialog());
        findViewById(R.id.btnManageSchools).setOnClickListener(v -> {
            new CustomSpellSchoolBottomSheet()
                    .show(getSupportFragmentManager(), "custom_schools");
        });
        findViewById(R.id.fabCreateSpell).setOnClickListener(v ->
                startActivity(new Intent(this, CustomSpellCreateActivity.class)));
    }

    private void setupRecyclerView() {
        RecyclerView recyclerView = findViewById(R.id.recycler_spells);
        adapter = new SpellAdapter(spell -> {
            if (spell.isCustom) {
                Intent intent = new Intent(this, CustomSpellDetailActivity.class);
                intent.putExtra("CUSTOM_SPELL_ID", spell.customId);
                startActivity(intent);
            } else {
                Intent intent = new Intent(this, SpellDetailActivity.class);
                intent.putExtra("SPELL_KEY", spell.key);
                startActivity(intent);
            }
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void setupSearch() {
        SearchView searchView = findViewById(R.id.search_view);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                viewModel.setQuery(query);
                return true;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                viewModel.setQuery(newText);
                return true;
            }
        });
    }

    private void setupObservers() {
        tvActiveFilters = findViewById(R.id.tv_active_filters);

        viewModel.getSpells().observe(this, spells -> {
            adapter.submitList(spells);
            ((TextView) findViewById(R.id.tv_spell_count))
                    .setText(spells.size() + " spells");
        });

        viewModel.getFilter().observe(this, filter -> {
            if (filter.isEmpty()) {
                tvActiveFilters.setVisibility(View.GONE);
            } else {
                tvActiveFilters.setVisibility(View.VISIBLE);
                tvActiveFilters.setText(buildFilterSummary(filter));
            }
        });

        viewModel.getSources().observe(this, sources -> {
            if (sources == null || sources.isEmpty()) return;
            buildSourceChips(sources);
        });
    }

    private void buildSourceChips(List<String> sources) {
        int chipCount = chipGroupSources.getChildCount();
        if (chipCount > 1) chipGroupSources.removeViews(1, chipCount - 1);

        for (String source : sources) {
            Chip chip = new Chip(this);
            chip.setText(formatSourceName(source));
            chip.setTag(source);
            chip.setCheckable(true);
            chip.setChipBackgroundColorResource(android.R.color.transparent);
            chip.setCheckedIconVisible(true);

            chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    Chip chipAll = findViewById(R.id.chip_all);
                    if (chipAll != null) chipAll.setChecked(false);
                    viewModel.setSource((String) buttonView.getTag());
                }
            });
            chipGroupSources.addView(chip);
        }

        Chip chipAll = findViewById(R.id.chip_all);
        if (chipAll != null) {
            chipAll.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    for (int i = 1; i < chipGroupSources.getChildCount(); i++) {
                        ((Chip) chipGroupSources.getChildAt(i)).setChecked(false);
                    }
                    viewModel.setSource("");
                }
            });
        }
    }

    private String formatSourceName(String source) {
        if (source == null) return "Unknown";
        if (source.equals("custom")) return "Custom";
        if (source.contains("System Reference Document 5.1")) return "SRD 5.1";
        if (source.contains("System Reference Document 5.2")) return "SRD 5.2";
        if (source.contains("Adventurer's Guide")) return "A5e AG";
        if (source.contains("Level Up")) return "Level Up";
        if (source.contains("Tome of Beasts")) return "ToB";
        return source.length() > 12 ? source.substring(0, 12) + "…" : source;
    }

    private String buildFilterSummary(SpellFilter filter) {
        List<String> parts = new ArrayList<>();
        if (filter.level >= 0) parts.add(filter.level == 0 ? "Cantrip" : "Level " + filter.level);
        if (!filter.schoolKey.isEmpty()) parts.add(filter.schoolKey);
        if (filter.ritualOnly) parts.add("Ritual");
        if (filter.concentrationOnly) parts.add("Concentration");
        if (!filter.query.isEmpty()) parts.add("\"" + filter.query + "\"");
        return "Filters: " + String.join(", ", parts);
    }

    private void showFilterDialog() {
        List<String> allNames = viewModel.getAllSchoolNames().getValue();
        SpellFilter current = viewModel.getFilter().getValue();
        if (current == null) current = new SpellFilter();

        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_spell_filter, null);
        Spinner spinnerLevel = dialogView.findViewById(R.id.spinner_level);
        Spinner spinnerSchool = dialogView.findViewById(R.id.spinner_school);
        CheckBox cbRitual = dialogView.findViewById(R.id.cb_ritual);
        CheckBox cbConcentration = dialogView.findViewById(R.id.cb_concentration);

        List<String> levelLabels = new ArrayList<>();
        List<Integer> levelValues = new ArrayList<>();
        levelLabels.add("All Levels"); levelValues.add(-1);
        levelLabels.add("Cantrip"); levelValues.add(0);
        for (int i = 1; i <= 9; i++) { levelLabels.add("Level " + i); levelValues.add(i); }
        spinnerLevel.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, levelLabels));
        int li = levelValues.indexOf(current.level);
        if (li >= 0) spinnerLevel.setSelection(li);

        List<String> schoolLabels = new ArrayList<>();
        List<String> schoolKeys = new ArrayList<>();
        schoolLabels.add("All Schools"); schoolKeys.add("");
        if (allNames != null) {
            for (String name : allNames) {
                if (name.isEmpty()) continue;
                schoolLabels.add(name);
                schoolKeys.add(name.toLowerCase());
            }
        }
        spinnerSchool.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, schoolLabels));
        int si = schoolKeys.indexOf(current.schoolKey);
        if (si >= 0) spinnerSchool.setSelection(si);

        cbRitual.setChecked(current.ritualOnly);
        cbConcentration.setChecked(current.concentrationOnly);

        new AlertDialog.Builder(this)
                .setTitle("Filter Spells")
                .setView(dialogView)
                .setPositiveButton("Apply", (d, w) -> {
                    viewModel.setLevel(levelValues.get(spinnerLevel.getSelectedItemPosition()));
                    viewModel.setSchool(schoolKeys.get(spinnerSchool.getSelectedItemPosition()));
                    viewModel.setRitualOnly(cbRitual.isChecked());
                    viewModel.setConcentrationOnly(cbConcentration.isChecked());
                })
                .setNeutralButton("Clear", (d, w) -> viewModel.clearFilters())
                .setNegativeButton("Cancel", null)
                .show();
    }
}