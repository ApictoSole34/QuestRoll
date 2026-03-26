package com.fizzycoyote.qusetroll.feature_item.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
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
import com.fizzycoyote.qusetroll.feature_item.adapter.ItemAdapter;
import com.fizzycoyote.qusetroll.feature_item.model.ItemFilter;
import com.fizzycoyote.qusetroll.feature_item.view_model.ItemListViewModel;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.List;

public class ItemListActivity extends AppCompatActivity {

    private ItemListViewModel viewModel;
    private ItemAdapter adapter;
    private RecyclerView rv;
    private ChipGroup chipGroupSources;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_list);

        Open5eDatabase open5eDb = Open5eDatabase.getInstance(this);
        UserContentDatabase customDb = UserContentDatabase.getInstance(this);

        viewModel = new ViewModelProvider(this,
                new ItemListViewModel.Factory(
                        open5eDb.itemDao(),
                        customDb.customItemDao()
                )).get(ItemListViewModel.class);

        chipGroupSources = findViewById(R.id.chip_group_sources);
        findViewById(R.id.btn_filter).setOnClickListener(v -> showFilterDialog());
        rv = findViewById(R.id.recycler_items);

        setupRecyclerView();
        setupSearch();
        setupObservers();
        viewModel.loadSourcesAndCategories();

        findViewById(R.id.fabCreateItem).setOnClickListener(v ->
                startActivity(new Intent(this, CustomItemCreateActivity.class)));
    }

    private void showFilterDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_item_filter, null);
        Spinner spinnerCategory = dialogView.findViewById(R.id.spinner_category);
        CheckBox cbMagicOnly = dialogView.findViewById(R.id.cb_magic_only);
        Spinner spinnerRarity = dialogView.findViewById(R.id.spinner_rarity);
        CheckBox cbAttunement = dialogView.findViewById(R.id.cb_attunement);

        List<String> categories = viewModel.getCategories().getValue();
        if (categories == null) categories = new ArrayList<>();
        List<String> categoryList = new ArrayList<>();
        categoryList.add("Any");
        categoryList.addAll(categories);
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categoryList);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(categoryAdapter);

        String[] rarities = {"Any", "Common", "Uncommon", "Rare", "Very Rare", "Legendary", "Artifact"};
        ArrayAdapter<String> rarityAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, rarities);
        rarityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRarity.setAdapter(rarityAdapter);

        ItemFilter currentFilter = viewModel.getFilter().getValue();
        if (currentFilter != null) {
            int catPos = 0;
            if (!currentFilter.categoryName.isEmpty()) {
                for (int i = 1; i < categoryList.size(); i++) {
                    if (categoryList.get(i).equals(currentFilter.categoryName)) {
                        catPos = i;
                        break;
                    }
                }
            }
            spinnerCategory.setSelection(catPos);

            cbMagicOnly.setChecked(currentFilter.magicOnly);

            int rarityPos = 0;
            if (!currentFilter.rarity.isEmpty()) {
                for (int i = 1; i < rarities.length; i++) {
                    if (rarities[i].equals(currentFilter.rarity)) {
                        rarityPos = i;
                        break;
                    }
                }
            }
            spinnerRarity.setSelection(rarityPos);

            cbAttunement.setChecked(currentFilter.requiresAttunement);
        }

        new AlertDialog.Builder(this)
                .setTitle("Filter Items")
                .setView(dialogView)
                .setPositiveButton("Apply", (d, w) -> {
                    ItemFilter f = new ItemFilter();
                    ItemFilter old = viewModel.getFilter().getValue();
                    if (old != null) {
                        f.query = old.query;
                        f.source = old.source;
                    }
                    String selectedCategory = (String) spinnerCategory.getSelectedItem();
                    f.categoryName = selectedCategory.equals("Any") ? "" : selectedCategory;
                    f.magicOnly = cbMagicOnly.isChecked();
                    String selectedRarity = (String) spinnerRarity.getSelectedItem();
                    f.rarity = selectedRarity.equals("Any") ? "" : selectedRarity;
                    f.requiresAttunement = cbAttunement.isChecked();

                    viewModel.applyFilter(f);
                })
                .setNeutralButton("Clear", (d, w) -> viewModel.clearFilters())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void setupRecyclerView() {
        adapter = new ItemAdapter(item -> {
            if (item.isCustom) {
                Intent i = new Intent(this, CustomItemDetailActivity.class);
                i.putExtra("CUSTOM_ITEM_ID", item.customId);
                startActivity(i);
            } else {
                Intent i = new Intent(this, ItemDetailActivity.class);
                i.putExtra("ITEM_KEY", item.key);
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
        viewModel.getItems().observe(this, items -> {
            adapter.submitList(items, () -> {
                rv.post(() -> rv.scrollToPosition(0));
            });
            ((TextView) findViewById(R.id.tv_item_count)).setText(items.size() + " items");
        });

        viewModel.getFilter().observe(this, f -> {
            TextView tv = findViewById(R.id.tv_active_filters);
            if (f.isEmpty()) {
                tv.setVisibility(View.GONE);
            } else {
                tv.setVisibility(View.VISIBLE);
                List<String> parts = new ArrayList<>();
                if (!f.categoryName.isEmpty()) parts.add(f.categoryName);
                if (!f.source.isEmpty()) parts.add(f.source);
                if (f.magicOnly) parts.add("Magic");
                if (!f.rarity.isEmpty()) parts.add(f.rarity);
                if (f.requiresAttunement) parts.add("Attunement");
                if (!f.query.isEmpty()) parts.add("\"" + f.query + "\"");
                tv.setText("Filters: " + String.join(", ", parts));
            }
        });

        viewModel.getSources().observe(this, sources -> {
            if (sources == null || sources.isEmpty()) return;
            buildSourceChips(sources);
        });

        viewModel.getCategories().observe(this, categories -> {
            if (categories == null) return;
            List<String> list = new ArrayList<>();
            list.add("All");
            list.addAll(categories);
        });
    }

    private void buildSourceChips(List<String> sources) {
        chipGroupSources.removeAllViews();
        Chip chipAll = new Chip(this);
        chipAll.setText("All");
        chipAll.setCheckable(true);
        chipAll.setChecked(true);
        chipAll.setTag("");
        chipAll.setOnCheckedChangeListener((btn, isChecked) -> {
            if (isChecked) {
                for (int i = 1; i < chipGroupSources.getChildCount(); i++)
                    ((Chip) chipGroupSources.getChildAt(i)).setChecked(false);
                viewModel.setSource("");
            }
        });
        chipGroupSources.addView(chipAll);

        for (String source : sources) {
            Chip chip = new Chip(this);
            chip.setText(formatSource(source));
            chip.setTag(source);
            chip.setCheckable(true);
            chip.setOnCheckedChangeListener((btn, isChecked) -> {
                if (isChecked) {
                    chipAll.setChecked(false);
                    viewModel.setSource((String) btn.getTag());
                }
            });
            chipGroupSources.addView(chip);
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

}