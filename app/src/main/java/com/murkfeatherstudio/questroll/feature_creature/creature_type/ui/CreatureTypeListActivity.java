package com.murkfeatherstudio.questroll.feature_creature.creature_type.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.SearchView;

import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.murkfeatherstudio.questroll.core.base.BaseActivity;
import com.murkfeatherstudio.questroll.core.local_database.Open5eDatabase;
import com.murkfeatherstudio.questroll.core.local_database.UserContentDatabase;
import com.murkfeatherstudio.questroll.databinding.ActivityCreatureTypeListBinding;
import com.murkfeatherstudio.questroll.feature_creature.creature_type.adapter.CreatureTypeAdapter;
import com.murkfeatherstudio.questroll.feature_creature.creature_type.view_model.CreatureTypeListViewModel;
import com.google.android.material.chip.Chip;

public class CreatureTypeListActivity extends BaseActivity {
    private CreatureTypeListViewModel viewModel;
    private CreatureTypeAdapter adapter;
    private ActivityCreatureTypeListBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCreatureTypeListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Open5eDatabase open5eDb = Open5eDatabase.getInstance(this);
        UserContentDatabase customDb = UserContentDatabase.getInstance(this);

        viewModel = new ViewModelProvider(this,
                new CreatureTypeListViewModel.Factory(open5eDb.creatureTypeDao(), customDb.customCreatureTypeDao()))
                .get(CreatureTypeListViewModel.class);

        setupRecyclerView();
        setupSearch();
        setupSourcesObserver();

        binding.fabCreate.setOnClickListener(v -> startActivity(new Intent(this, CustomCreatureTypeCreateActivity.class)));
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
        binding.recyclerCreatureTypes.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerCreatureTypes.setAdapter(adapter);
        binding.recyclerCreatureTypes.setSaveEnabled(false);
    }

    private void setupSearch() {
        binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String q) { viewModel.setQuery(q); return true; }
            @Override
            public boolean onQueryTextChange(String q) { viewModel.setQuery(q); return true; }
        });
    }

    /**
     * NOTE: chip_group_sources is managed dynamically (addView()).
     * Chips are created in code based on data from the ViewModel. ViewBinding 
     * does not apply to these dynamically added elements.
     */
    private void setupSourcesObserver() {
        viewModel.getSources().observe(this, sources -> {
            binding.chipGroupSources.removeAllViews();

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
            binding.chipGroupSources.addView(chipAll);

            Chip chipCustom = new Chip(this);
            chipCustom.setText("Custom");
            chipCustom.setCheckable(true);
            chipCustom.setTag("custom");
            chipCustom.setOnCheckedChangeListener((btn, isChecked) -> {
                if (isChecked) {
                    chipAll.setChecked(false);
                    for (int i = 2; i < binding.chipGroupSources.getChildCount(); i++) {
                        ((Chip) binding.chipGroupSources.getChildAt(i)).setChecked(false);
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
            binding.chipGroupSources.addView(chipCustom);

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
                binding.chipGroupSources.addView(chip);
            }
        });

        viewModel.getCustomOnly().observe(this, isCustomOnly -> {
            if (isCustomOnly) {
                for (int i = 0; i < binding.chipGroupSources.getChildCount(); i++) {
                    Chip chip = (Chip) binding.chipGroupSources.getChildAt(i);
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
            binding.tvCount.setText(list.size() + " creature types");
        });
    }
}