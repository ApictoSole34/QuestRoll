package com.fizzycoyote.qusetroll.feature_rule.rule_set.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fizzycoyote.qusetroll.R;
import com.fizzycoyote.qusetroll.core.local_database.Open5eDatabase;
import com.fizzycoyote.qusetroll.feature_rule.rule.ui.RuleListActivity;
import com.fizzycoyote.qusetroll.feature_rule.rule_set.adapter.RulesetAdapter;
import com.fizzycoyote.qusetroll.feature_rule.rule_set.view_model.RulesetListViewModel;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

public class RulesetListActivity extends AppCompatActivity {
    private RulesetListViewModel viewModel;
    private RulesetAdapter adapter;
    private RecyclerView rv;
    private ChipGroup chipGroupSources;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ruleset_list);

        chipGroupSources = findViewById(R.id.chip_group_sources);
        rv = findViewById(R.id.recycler_rulesets);
        setupRecyclerView();
        setupSearch();

        viewModel = new ViewModelProvider(this,
                new RulesetListViewModel.Factory(Open5eDatabase.getInstance(this).rulesetDao()))
                .get(RulesetListViewModel.class);

        setupSourcesObserver();
        viewModel.getFilteredRulesets().observe(this, rulesets -> {
            adapter.submitList(rulesets);
            ((TextView) findViewById(R.id.tv_count)).setText(rulesets.size() + " rulesets");
        });
    }

    private void setupRecyclerView() {
        adapter = new RulesetAdapter(ruleset -> {
            Intent i = new Intent(this, RuleListActivity.class);
            i.putExtra("RULESET_KEY", ruleset.key);
            startActivity(i);
        });
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adapter);
        rv.setSaveEnabled(false);
    }

    private void setupSearch() {
        SearchView searchView = findViewById(R.id.search_view);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override public boolean onQueryTextSubmit(String q) { viewModel.setQuery(q); return true; }
            @Override public boolean onQueryTextChange(String q) { viewModel.setQuery(q); return true; }
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
                if (isChecked) viewModel.setSelectedSource("");
            });
            chipGroupSources.addView(chipAll);
            if (sources == null) return;
            for (String source : sources) {
                Chip chip = new Chip(this);
                chip.setText(source);
                chip.setTag(source);
                chip.setCheckable(true);
                chip.setOnCheckedChangeListener((btn, isChecked) -> {
                    if (isChecked) {
                        chipAll.setChecked(false);
                        viewModel.setSelectedSource(source);
                    }
                });
                chipGroupSources.addView(chip);
            }
        });
    }
}