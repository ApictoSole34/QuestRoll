package com.fizzycoyote.qusetroll.feature_item.ui.weapon;

import static androidx.core.content.ContextCompat.startActivity;

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
import com.fizzycoyote.qusetroll.feature_item.adapter.weapon.WeaponAdapter;
import com.fizzycoyote.qusetroll.feature_item.model.weapon.WeaponFilter;
import com.fizzycoyote.qusetroll.feature_item.view_model.weapon.WeaponListViewModel;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.List;

public class WeaponListActivity extends AppCompatActivity {

    private WeaponListViewModel viewModel;
    private WeaponAdapter adapter;
    private RecyclerView rv;
    private ChipGroup chipGroupSources;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weapon_list);

        Open5eDatabase open5eDb = Open5eDatabase.getInstance(this);
        UserContentDatabase customDb = UserContentDatabase.getInstance(this);

        viewModel = new ViewModelProvider(this,
                new WeaponListViewModel.Factory(
                        open5eDb.weaponDao(),
                        customDb.customWeaponDao()
                )).get(WeaponListViewModel.class);

        chipGroupSources = findViewById(R.id.chip_group_sources);
        rv = findViewById(R.id.recycler_weapons);

        setupRecyclerView();
        setupSearch();
        setupObservers();
        viewModel.loadSources();

        findViewById(R.id.btnFilter).setOnClickListener(v -> showFilterDialog());
        findViewById(R.id.fabCreateWeapon).setOnClickListener(v ->
                startActivity(new Intent(this, CustomWeaponCreateActivity.class)));
    }

    private void setupRecyclerView() {
        adapter = new WeaponAdapter(weapon -> {
            if (weapon.isCustom) {
                Intent i = new Intent(this, CustomWeaponDetailActivity.class);
                i.putExtra("CUSTOM_WEAPON_ID", weapon.customId);
                startActivity(i);
            } else {
                Intent i = new Intent(this, WeaponDetailActivity.class);
                i.putExtra("WEAPON_KEY", weapon.key);
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
        viewModel.getWeapons().observe(this, weapons -> {
            adapter.submitList(weapons, () -> rv.scrollToPosition(0));
            ((TextView) findViewById(R.id.tv_weapon_count))
                    .setText(weapons.size() + " weapons");
        });

        viewModel.getFilter().observe(this, f -> {
            TextView tv = findViewById(R.id.tv_active_filters);
            if (f.isEmpty()) { tv.setVisibility(View.GONE); }
            else {
                tv.setVisibility(View.VISIBLE);
                List<String> parts = new ArrayList<>();
                if (f.simpleOnly) parts.add("Simple");
                if (f.martialOnly) parts.add("Martial");
                if (!f.source.isEmpty()) parts.add(f.source);
                if (!f.query.isEmpty()) parts.add("\"" + f.query + "\"");
                tv.setText("Filters: " + String.join(", ", parts));
            }
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
        if (source.contains("Adventurer's Guide")) return "A5e AG";
        return source.length() > 12 ? source.substring(0, 12) + "…" : source;
    }

    private void showFilterDialog() {
        View dv = LayoutInflater.from(this).inflate(R.layout.dialog_weapon_filter, null);
        CheckBox cbSimple = dv.findViewById(R.id.cb_simple_only);
        CheckBox cbMartial = dv.findViewById(R.id.cb_martial_only);

        WeaponFilter current = viewModel.getFilter().getValue();
        if (current == null) current = new WeaponFilter();
        cbSimple.setChecked(current.simpleOnly);
        cbMartial.setChecked(current.martialOnly);

        cbSimple.setOnCheckedChangeListener((v, c) -> { if (c) cbMartial.setChecked(false); });
        cbMartial.setOnCheckedChangeListener((v, c) -> { if (c) cbSimple.setChecked(false); });

        new AlertDialog.Builder(this)
                .setTitle("Filter Weapons")
                .setView(dv)
                .setPositiveButton("Apply", (d, w) -> {
                    WeaponFilter f = new WeaponFilter();
                    WeaponFilter old = viewModel.getFilter().getValue();
                    if (old != null) { f.query = old.query; f.source = old.source; }
                    f.simpleOnly = cbSimple.isChecked();
                    f.martialOnly = cbMartial.isChecked();
                    viewModel.applyFilter(f);
                })
                .setNeutralButton("Clear", (d, w) -> viewModel.clearFilters())
                .setNegativeButton("Cancel", null)
                .show();
    }
}