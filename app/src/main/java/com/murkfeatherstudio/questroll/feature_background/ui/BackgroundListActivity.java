package com.murkfeatherstudio.questroll.feature_background.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.SearchView;

import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.murkfeatherstudio.questroll.core.base.BaseActivity;
import com.murkfeatherstudio.questroll.core.local_database.Open5eDatabase;
import com.murkfeatherstudio.questroll.core.local_database.UserContentDatabase;
import com.murkfeatherstudio.questroll.databinding.ActivityBackgroundListBinding;
import com.murkfeatherstudio.questroll.feature_background.adapter.BackgroundAdapter;
import com.murkfeatherstudio.questroll.feature_background.view_model.BackgroundListViewModel;
import com.google.android.material.chip.Chip;

import java.util.List;

public class BackgroundListActivity extends BaseActivity {

    private BackgroundListViewModel viewModel;
    private BackgroundAdapter adapter;
    private ActivityBackgroundListBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBackgroundListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Open5eDatabase open5eDb = Open5eDatabase.getInstance(this);
        UserContentDatabase customDb = UserContentDatabase.getInstance(this);

        viewModel = new ViewModelProvider(this,
                new BackgroundListViewModel.Factory(
                        open5eDb.backgroundDao(),
                        customDb.customBackgroundDao()
                )).get(BackgroundListViewModel.class);

        setupRecyclerView();
        setupSearch();
        setupObservers();
        viewModel.loadSources();

        binding.fabCreateBackground.setOnClickListener(v ->
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
        binding.recyclerBackgrounds.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerBackgrounds.setAdapter(adapter);
    }

    private void setupSearch() {
        binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override public boolean onQueryTextSubmit(String q) { viewModel.setQuery(q); return true; }
            @Override public boolean onQueryTextChange(String q) { viewModel.setQuery(q); return true; }
        });
    }

    private void setupObservers() {
        viewModel.getBackgrounds().observe(this, backgrounds -> {
            adapter.submitList(backgrounds, () -> binding.recyclerBackgrounds.scrollToPosition(0));
            binding.tvBackgroundCount.setText(backgrounds.size() + " backgrounds");
        });

        viewModel.getSources().observe(this, sources -> {
            if (sources == null || sources.isEmpty()) return;
            buildSourceChips(sources);
        });
    }

    /**
     * NOTE: chip_group_sources is managed dynamically (addView()).
     * Source chips are created at runtime based on data from the database.
     * ViewBinding is not applicable to these dynamically added elements.
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
        if (source.contains("Adventurer's Guide")) return "A5e AG";
        if (source.contains("System Reference Document 5.1")) return "SRD 5.1";
        if (source.contains("System Reference Document 5.2")) return "SRD 5.2";
        return source.length() > 12 ? source.substring(0, 12) + "…" : source;
    }
}