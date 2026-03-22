package com.fizzycoyote.qusetroll.feature_background.ui;

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
import com.fizzycoyote.qusetroll.core.local_database.UserContentDatabase;
import com.fizzycoyote.qusetroll.feature_background.adapter.BackgroundAdapter;
import com.fizzycoyote.qusetroll.feature_background.view_model.BackgroundListViewModel;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.List;

public class BackgroundListActivity extends AppCompatActivity {

    private BackgroundListViewModel viewModel;
    private BackgroundAdapter adapter;
    private RecyclerView rv;
    private ChipGroup chipGroupSources;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_background_list);

        Open5eDatabase open5eDb = Open5eDatabase.getInstance(this);
        UserContentDatabase customDb = UserContentDatabase.getInstance(this);

        viewModel = new ViewModelProvider(this,
                new BackgroundListViewModel.Factory(
                        open5eDb.backgroundDao(),
                        customDb.customBackgroundDao()
                )).get(BackgroundListViewModel.class);

        chipGroupSources = findViewById(R.id.chip_group_sources);
        rv = findViewById(R.id.recycler_backgrounds);

        setupRecyclerView();
        setupSearch();
        setupObservers();
        viewModel.loadSources();

        findViewById(R.id.fabCreateBackground).setOnClickListener(v ->
                startActivity(new Intent(this, CustomBackgroundCreateActivity.class)));
    }

    private void setupRecyclerView() {
        adapter = new BackgroundAdapter(background -> {
            if (background.isCustom) {
                Intent i = new Intent(this, CustomBackgroundDetailActivity.class);
                i.putExtra("CUSTOM_BACKGROUND_ID", background.customId);
                startActivity(i);
            } else {
                Intent i = new Intent(this, BackgroundDetailActivity.class);
                i.putExtra("BACKGROUND_KEY", background.key);
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
        viewModel.getBackgrounds().observe(this, backgrounds -> {
            adapter.submitList(backgrounds, () -> rv.scrollToPosition(0));
            ((TextView) findViewById(R.id.tv_background_count))
                    .setText(backgrounds.size() + " backgrounds");
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
        if (source.contains("Adventurer's Guide")) return "A5e AG";
        if (source.contains("System Reference Document 5.1")) return "SRD 5.1";
        if (source.contains("System Reference Document 5.2")) return "SRD 5.2";
        return source.length() > 12 ? source.substring(0, 12) + "…" : source;
    }
}