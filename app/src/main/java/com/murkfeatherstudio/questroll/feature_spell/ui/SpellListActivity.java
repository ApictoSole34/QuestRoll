package com.murkfeatherstudio.questroll.feature_spell.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;

import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.murkfeatherstudio.questroll.R;
import com.murkfeatherstudio.questroll.core.base.BaseActivity;
import com.murkfeatherstudio.questroll.core.local_database.Open5eDatabase;
import com.murkfeatherstudio.questroll.core.local_database.UserContentDatabase;
import com.murkfeatherstudio.questroll.databinding.ActivitySpellListBinding;
import com.murkfeatherstudio.questroll.databinding.DialogSpellFilterBinding;
import com.murkfeatherstudio.questroll.feature_spell.spell_school.ui.SpellSchoolListActivity;
import com.murkfeatherstudio.questroll.feature_spell.view_model.SpellListViewModel;
import com.murkfeatherstudio.questroll.feature_spell.adapter.SpellAdapter;
import com.murkfeatherstudio.questroll.feature_spell.model.SpellFilter;
import com.google.android.material.chip.Chip;

import java.util.ArrayList;
import java.util.List;

public class SpellListActivity extends BaseActivity {

    private SpellListViewModel viewModel;
    private SpellAdapter adapter;
    private ActivitySpellListBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySpellListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Open5eDatabase open5eDb = Open5eDatabase.getInstance(this);
        UserContentDatabase customDb = UserContentDatabase.getInstance(this);

        viewModel = new ViewModelProvider(this,
                new SpellListViewModel.Factory(
                        open5eDb.spellDao(),
                        open5eDb.spellSchoolDao(),
                        customDb.customSpellDao(),
                        customDb.customSpellSchoolDao()
                )).get(SpellListViewModel.class);

        setupRecyclerView();
        setupSearch();
        setupObservers();
        viewModel.loadSchools();
        viewModel.loadSources();

        binding.btnFilter.setOnClickListener(v -> showFilterDialog());
        binding.btnManageSchools.setOnClickListener(v ->
                startActivity(new Intent(this, SpellSchoolListActivity.class)));
        binding.fabCreateSpell.setOnClickListener(v ->
                startActivity(new Intent(this, CustomSpellCreateActivity.class)));
    }

    private void setupRecyclerView() {
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
        binding.recyclerSpells.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerSpells.setAdapter(adapter);
    }

    private void setupSearch() {
        binding.searchView.setOnQueryTextListener(new android.widget.SearchView.OnQueryTextListener() {
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
        viewModel.getSpells().observe(this, spells -> {
            adapter.submitList(spells);
            binding.tvSpellCount.setText(spells.size() + " spells");
        });

        viewModel.getFilter().observe(this, filter -> {
            if (filter.isEmpty()) {
                binding.tvActiveFilters.setVisibility(View.GONE);
            } else {
                binding.tvActiveFilters.setVisibility(View.VISIBLE);
                binding.tvActiveFilters.setText(buildFilterSummary(filter));
            }
        });

        viewModel.getSources().observe(this, sources -> {
            if (sources == null || sources.isEmpty()) return;
            buildSourceChips(sources);
        });
    }

    /**
     * JAVADOC: chipGroupSources is a dynamic container. We use removeViews() and addView() 
     * to manage Material Chips programmatically because the spell sources are loaded 
     * from the database at runtime. Static View Binding is not applicable here.
     */
    private void buildSourceChips(List<String> sources) {
        int chipCount = binding.chipGroupSources.getChildCount();
        if (chipCount > 1) binding.chipGroupSources.removeViews(1, chipCount - 1);

        for (String source : sources) {
            Chip chip = new Chip(this);
            chip.setText(formatSourceName(source));
            chip.setTag(source);
            chip.setCheckable(true);
            chip.setChipBackgroundColorResource(android.R.color.transparent);
            chip.setCheckedIconVisible(true);

            chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    binding.chipAll.setChecked(false);
                    viewModel.setSource((String) buttonView.getTag());
                }
            });
            binding.chipGroupSources.addView(chip);
        }

        binding.chipAll.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                for (int i = 1; i < binding.chipGroupSources.getChildCount(); i++) {
                    ((Chip) binding.chipGroupSources.getChildAt(i)).setChecked(false);
                }
                viewModel.setSource("");
            }
        });
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

        DialogSpellFilterBinding filterBinding = DialogSpellFilterBinding.inflate(getLayoutInflater());

        List<String> levelLabels = new ArrayList<>();
        List<Integer> levelValues = new ArrayList<>();
        levelLabels.add("All Levels"); levelValues.add(-1);
        levelLabels.add("Cantrip"); levelValues.add(0);
        for (int i = 1; i <= 9; i++) { levelLabels.add("Level " + i); levelValues.add(i); }
        filterBinding.spinnerLevel.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, levelLabels));
        int li = levelValues.indexOf(current.level);
        if (li >= 0) filterBinding.spinnerLevel.setSelection(li);

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
        filterBinding.spinnerSchool.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, schoolLabels));
        int si = schoolKeys.indexOf(current.schoolKey);
        if (si >= 0) filterBinding.spinnerSchool.setSelection(si);

        filterBinding.cbRitual.setChecked(current.ritualOnly);
        filterBinding.cbConcentration.setChecked(current.concentrationOnly);

        new AlertDialog.Builder(this)
                .setTitle("Filter Spells")
                .setView(filterBinding.getRoot())
                .setPositiveButton("Apply", (d, w) -> {
                    viewModel.setLevel(levelValues.get(filterBinding.spinnerLevel.getSelectedItemPosition()));
                    viewModel.setSchool(schoolKeys.get(filterBinding.spinnerSchool.getSelectedItemPosition()));
                    viewModel.setRitualOnly(filterBinding.cbRitual.isChecked());
                    viewModel.setConcentrationOnly(filterBinding.cbConcentration.isChecked());
                })
                .setNeutralButton("Clear", (d, w) -> viewModel.clearFilters())
                .setNegativeButton("Cancel", null)
                .show();
    }
}
