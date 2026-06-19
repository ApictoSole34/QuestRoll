package com.fizzycoyote.qusetroll.feature_condition.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.SearchView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.fizzycoyote.qusetroll.R;
import com.fizzycoyote.qusetroll.core.base.BaseActivity;
import com.fizzycoyote.qusetroll.core.local_database.Open5eDatabase;
import com.fizzycoyote.qusetroll.core.local_database.UserContentDatabase;
import com.fizzycoyote.qusetroll.feature_condition.adapter.ConditionAdapter;
import com.fizzycoyote.qusetroll.feature_condition.view_model.ConditionListViewModel;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class ConditionListActivity extends BaseActivity {
    private ConditionListViewModel viewModel;
    private ConditionAdapter adapter;
    private RecyclerView rv;
    private ChipGroup chipGroupSources;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_condition_list);

        Open5eDatabase open5eDb = Open5eDatabase.getInstance(this);
        UserContentDatabase customDb = UserContentDatabase.getInstance(this);

        viewModel = new ViewModelProvider(this,
                new ConditionListViewModel.Factory(open5eDb.conditionDao(), customDb.customConditionDao()))
                .get(ConditionListViewModel.class);

        chipGroupSources = findViewById(R.id.chip_group_sources);
        setupRecyclerView();
        setupSearch();
        setupSourcesObserver();

        FloatingActionButton fab = findViewById(R.id.fabCreate);
        fab.setOnClickListener(v -> startActivity(new Intent(this, CustomConditionCreateActivity.class)));
    }

    private void setupRecyclerView() {
        adapter = new ConditionAdapter(condition -> {
            Intent i;
            if (condition.isCustom) {
                i = new Intent(this, CustomConditionDetailActivity.class);
                i.putExtra("CUSTOM_CONDITION_ID", condition.customId);
            } else {
                i = new Intent(this, ConditionDetailActivity.class);
                i.putExtra("CONDITION_KEY", condition.key);
            }
            startActivity(i);
        });
        rv = findViewById(R.id.recycler_conditions);
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
        viewModel.getConditions().observe(this, list -> {
            adapter.submitList(list);
            TextView tvCount = findViewById(R.id.tv_count);
            tvCount.setText(list.size() + " conditions");
        });
    }
}
