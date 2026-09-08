package com.murkfeatherstudio.questroll.feature_creature.creature_type.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.murkfeatherstudio.questroll.R;
import com.murkfeatherstudio.questroll.core.base.BaseActivity;
import com.murkfeatherstudio.questroll.core.local_database.Open5eDatabase;
import com.murkfeatherstudio.questroll.core.local_database.UserContentDatabase;
import com.murkfeatherstudio.questroll.feature_creature.creature_type.adapter.CreatureTypeAdapter;
import com.murkfeatherstudio.questroll.feature_creature.creature_type.view_model.CreatureTypeListViewModel;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class CreatureTypeListActivity extends BaseActivity {
    private CreatureTypeListViewModel viewModel;
    private CreatureTypeAdapter adapter;
    private RecyclerView rv;
    private ChipGroup chipGroupSources;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_creature_type_list);

        Open5eDatabase open5eDb = Open5eDatabase.getInstance(this);
        UserContentDatabase customDb = UserContentDatabase.getInstance(this);

        viewModel = new ViewModelProvider(this,
                new CreatureTypeListViewModel.Factory(open5eDb.creatureTypeDao(), customDb.customCreatureTypeDao()))
                .get(CreatureTypeListViewModel.class);

        chipGroupSources = findViewById(R.id.chip_group_sources);
        setupRecyclerView();
        setupSearch();
        setupSourcesObserver();

        FloatingActionButton fab = findViewById(R.id.fabCreate);
        fab.setOnClickListener(v -> startActivity(new Intent(this, CustomCreatureTypeCreateActivity.class)));
    }

    private void setupRecyclerView() {
        adapter = new CreatureTypeAdapter(type -> {
            Intent i;
            if (type.isCustom) {
                i = new Intent(this, CustomCreatureTypeDetailActivity.class);
                i.putExtra("CUSTOM_CREATURE_TYPE_ID", type.customId);
            } else {
                i = new Intent(this, CreatureTypeDetailActivity.class);
                i.putExtra("CREATURE_TYPE_KEY", type.key);
            }
            startActivity(i);
        });
        rv = findViewById(R.id.recycler_creature_types);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adapter);
        rv.setSaveEnabled(false);
    }

    private void setupSearch() {
        SearchView searchView = findViewById(R.id.search_view);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String q) { viewModel.setQuery(q); return true; }
            @Override
            public boolean onQueryTextChange(String q) { viewModel.setQuery(q); return true; }
        });
    }

    private void setupSourcesObserver() {
        viewModel.getSources().observe(this, sources -> {
            chipGroupSources.removeAllViews();

            Chip chipAll = new Chip(this);
            chipAll.setText("All");
            chipAll.setCheckable(true);
            chipAll.setChecked(true);
            chipAll.setTag("");
            chipAll.setOnCheckedChangeListener((btn, isChecked) -> {
                if (isChecked) {
                    viewModel.setSelectedSource("");
                    viewModel.setCustomOnly(false);
                }
            });
            chipGroupSources.addView(chipAll);

            Chip chipCustom = new Chip(this);
            chipCustom.setText("Custom");
            chipCustom.setCheckable(true);
            chipCustom.setTag("custom");
            chipCustom.setOnCheckedChangeListener((btn, isChecked) -> {
                if (isChecked) {
                    chipAll.setChecked(false);
                    for (int i = 2; i < chipGroupSources.getChildCount(); i++) {
                        ((Chip) chipGroupSources.getChildAt(i)).setChecked(false);
                    }
                    viewModel.setSelectedSource("");
                    viewModel.setCustomOnly(true);
                } else {
                    if (viewModel.getCustomOnly().getValue() != null && viewModel.getCustomOnly().getValue()) {
                        chipAll.setChecked(true);
                        viewModel.setCustomOnly(false);
                    }
                }
            });
            chipGroupSources.addView(chipCustom);

            if (sources == null) return;
            for (String source : sources) {
                Chip chip = new Chip(this);
                chip.setText(source);
                chip.setTag(source);
                chip.setCheckable(true);
                chip.setOnCheckedChangeListener((btn, isChecked) -> {
                    if (isChecked) {
                        chipAll.setChecked(false);
                        chipCustom.setChecked(false);
                        viewModel.setSelectedSource(source);
                        viewModel.setCustomOnly(false);
                    }
                });
                chipGroupSources.addView(chip);
            }
        });

        viewModel.getCustomOnly().observe(this, isCustomOnly -> {
            if (isCustomOnly) {
                for (int i = 0; i < chipGroupSources.getChildCount(); i++) {
                    Chip chip = (Chip) chipGroupSources.getChildAt(i);
                    if ("custom".equals(chip.getTag())) {
                        chip.setChecked(true);
                    } else {
                        chip.setChecked(false);
                    }
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        viewModel.getCreatureTypes().observe(this, list -> {
            adapter.submitList(list);
            TextView tvCount = findViewById(R.id.tv_count);
            tvCount.setText(list.size() + " creature types");
        });
    }
}