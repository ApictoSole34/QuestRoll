package com.murkfeatherstudio.questroll.feature_item.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.murkfeatherstudio.questroll.R;
import com.murkfeatherstudio.questroll.core.base.BaseActivity;
import com.murkfeatherstudio.questroll.core.local_database.Open5eDatabase;
import com.murkfeatherstudio.questroll.core.local_database.UserContentDatabase;
import com.murkfeatherstudio.questroll.databinding.ActivityItemListBinding;
import com.murkfeatherstudio.questroll.databinding.DialogItemFilterBinding;
import com.murkfeatherstudio.questroll.feature_item.adapter.ItemAdapter;
import com.murkfeatherstudio.questroll.feature_item.item_category.ui.ItemCategoryListActivity;
import com.murkfeatherstudio.questroll.feature_item.item_rarity.ui.ItemRarityListActivity;
import com.murkfeatherstudio.questroll.feature_item.model.ItemFilter;
import com.murkfeatherstudio.questroll.feature_item.view_model.ItemListViewModel;
import com.murkfeatherstudio.questroll.feature_item.weapon_property.ui.WeaponPropertyListActivity;
import com.google.android.material.chip.Chip;

import java.util.ArrayList;
import java.util.List;

public class ItemListActivity extends BaseActivity {

    private ItemListViewModel viewModel;
    private ItemAdapter adapter;
    private ActivityItemListBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityItemListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Open5eDatabase open5eDb = Open5eDatabase.getInstance(this);
        UserContentDatabase customDb = UserContentDatabase.getInstance(this);

        viewModel = new ViewModelProvider(this,
                new ItemListViewModel.Factory(
                        open5eDb.itemDao(),
                        customDb.customItemDao(),
                        open5eDb.itemCategoryDao(),
                        customDb.customItemCategoryDao(),
                        open5eDb.itemRarityDao(),
                        customDb.customItemRarityDao(),
                        open5eDb.weaponPropertyDao(),
                        customDb.customWeaponPropertyDao()
                )).get(ItemListViewModel.class);

        binding.btnFilter.setOnClickListener(v -> showFilterDialog());
        binding.btnManageCategories.setOnClickListener(v ->
                startActivity(new Intent(this, ItemCategoryListActivity.class)));

        setupRecyclerView();
        setupSearch();
        setupObservers();
        viewModel.loadSourcesAndCategories();
        setupCategorySpinner();

        binding.fabCreateItem.setOnClickListener(v ->
                startActivity(new Intent(this, CustomItemCreateActivity.class)));
    }

    private void setupCategorySpinner() {
        viewModel.getAllCategoryNames().observe(this, categoryNames -> {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_spinner_item, categoryNames);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            binding.spinnerCategories.setAdapter(adapter);
        });

        binding.spinnerCategories.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selected = (String) parent.getItemAtPosition(position);
                viewModel.setCategory(selected.equals("All") ? "" : selected);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void showFilterDialog() {
        DialogItemFilterBinding dialogBinding = DialogItemFilterBinding.inflate(getLayoutInflater());

        dialogBinding.btnManageRaritiesInDialog.setOnClickListener(v -> {
            startActivity(new Intent(this, ItemRarityListActivity.class));
        });
        dialogBinding.btnManageWeaponPropertiesInDialog.setOnClickListener(v -> {
            startActivity(new Intent(this, WeaponPropertyListActivity.class));
        });

        List<String> propertyNames = viewModel.getCombinedWeaponPropertyNamesForFilter().getValue();
        if (propertyNames == null) propertyNames = new ArrayList<>();
        ArrayAdapter<String> propertyAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, propertyNames);
        propertyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dialogBinding.spinnerWeaponProperty.setAdapter(propertyAdapter);

        List<String> rarityNames = viewModel.getCombinedRarityNamesForFilter().getValue();
        if (rarityNames == null) rarityNames = new ArrayList<>();
        ArrayAdapter<String> rarityAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, rarityNames);
        rarityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dialogBinding.spinnerRarity.setAdapter(rarityAdapter);

        ItemFilter currentFilter = viewModel.getFilter().getValue();
        if (currentFilter != null) {
            dialogBinding.cbMagicOnly.setChecked(currentFilter.magicOnly);
            int rarityPos = rarityNames.indexOf(currentFilter.rarity);
            if (rarityPos >= 0) dialogBinding.spinnerRarity.setSelection(rarityPos);
            dialogBinding.cbAttunement.setChecked(currentFilter.requiresAttunement);
            int propPos = propertyNames.indexOf(currentFilter.weaponProperty);
            if (propPos >= 0) dialogBinding.spinnerWeaponProperty.setSelection(propPos);
        }

        new AlertDialog.Builder(this)
                .setTitle("Filter Items")
                .setView(dialogBinding.getRoot())
                .setPositiveButton("Apply", (d, w) -> {
                    ItemFilter f = new ItemFilter();
                    ItemFilter old = viewModel.getFilter().getValue();
                    if (old != null) {
                        f.query = old.query;
                        f.source = old.source;
                        f.categoryName = old.categoryName;
                    }
                    String selectedProperty = (String) dialogBinding.spinnerWeaponProperty.getSelectedItem();
                    f.weaponProperty = selectedProperty.equals("Any") ? "" : selectedProperty;
                    String selectedRarity = (String) dialogBinding.spinnerRarity.getSelectedItem();
                    f.rarity = selectedRarity.equals("Any") ? "" : selectedRarity;
                    f.magicOnly = dialogBinding.cbMagicOnly.isChecked();
                    f.requiresAttunement = dialogBinding.cbAttunement.isChecked();
                    viewModel.applyFilter(f);
                })
                .setNeutralButton("Clear", (d, w) -> {
                    viewModel.clearFilters();
                    String currentCategory = binding.spinnerCategories.getSelectedItem().toString();
                    if (!currentCategory.equals("All")) viewModel.setCategory(currentCategory);
                })
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
        binding.recyclerItems.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerItems.setAdapter(adapter);
    }

    private void setupSearch() {
        binding.searchView.setOnQueryTextListener(new android.widget.SearchView.OnQueryTextListener() {
            @Override public boolean onQueryTextSubmit(String q) { viewModel.setQuery(q); return true; }
            @Override public boolean onQueryTextChange(String q) { viewModel.setQuery(q); return true; }
        });
    }

    private void setupObservers() {
        viewModel.getItems().observe(this, items -> {
            adapter.submitList(items, () -> {
                binding.recyclerItems.post(() -> binding.recyclerItems.scrollToPosition(0));
            });
            binding.tvItemCount.setText(items.size() + " items");
        });

        viewModel.getFilter().observe(this, f -> {
            if (f.isEmpty()) {
                binding.tvActiveFilters.setVisibility(View.GONE);
            } else {
                binding.tvActiveFilters.setVisibility(View.VISIBLE);
                List<String> parts = new ArrayList<>();
                if (!f.categoryName.isEmpty()) parts.add(f.categoryName);
                if (!f.source.isEmpty()) parts.add(f.source);
                if (f.magicOnly) parts.add("Magic");
                if (!f.rarity.isEmpty()) parts.add(f.rarity);
                if (f.requiresAttunement) parts.add("Attunement");
                if (!f.weaponProperty.isEmpty()) parts.add("Prop: " + f.weaponProperty);
                if (!f.query.isEmpty()) parts.add("\"" + f.query + "\"");
                binding.tvActiveFilters.setText("Filters: " + String.join(", ", parts));
            }
        });

        viewModel.getSources().observe(this, sources -> {
            if (sources == null || sources.isEmpty()) return;
            buildSourceChips(sources);
        });
    }

    /**
     * JAVADOC: chipGroupSources is populated dynamically with Material Chips. 
     * Since the number and labels of chips depend on the data sources returned 
     * by the ViewModel at runtime, these views are created programmatically 
     * and added via addView(). View Binding cannot reference these dynamic children.
     */
    private void buildSourceChips(List<String> sources) {
        binding.chipGroupSources.removeAllViews();
        Chip chipAll = new Chip(this);
        chipAll.setText("All");
        chipAll.setCheckable(true);
        chipAll.setChecked(true);
        chipAll.setTag("");
        chipAll.setOnCheckedChangeListener((btn, isChecked) -> {
            if (isChecked) {
                for (int i = 1; i < binding.chipGroupSources.getChildCount(); i++)
                    ((Chip) binding.chipGroupSources.getChildAt(i)).setChecked(false);
                viewModel.setSource("");
            }
        });
        binding.chipGroupSources.addView(chipAll);

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
            binding.chipGroupSources.addView(chip);
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
