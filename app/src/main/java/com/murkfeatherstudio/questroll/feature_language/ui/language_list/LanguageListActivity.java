package com.murkfeatherstudio.questroll.feature_language.ui.language_list;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.SearchView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.murkfeatherstudio.questroll.core.base.BaseActivity;
import com.murkfeatherstudio.questroll.core.local_database.Open5eDatabase;
import com.murkfeatherstudio.questroll.core.local_database.UserContentDatabase;
import com.murkfeatherstudio.questroll.databinding.ActivityLanguageListBinding;
import com.murkfeatherstudio.questroll.databinding.DialogLanguageFilterBinding;
import com.murkfeatherstudio.questroll.feature_language.data.repository.LanguageRepository;
import com.murkfeatherstudio.questroll.feature_language.ui.language_create.CustomLanguageCreateActivity;
import com.murkfeatherstudio.questroll.feature_language.ui.language_details.LanguageDetailActivity;
import com.murkfeatherstudio.questroll.feature_language.ui.language_list.adapter.LanguageListAdapter;
import com.murkfeatherstudio.questroll.feature_language.model.CombinedLanguage;
import com.murkfeatherstudio.questroll.feature_language.view_model.LanguageListViewModel;
import com.murkfeatherstudio.questroll.feature_language.view_model.ViewModelFactory;

import java.util.ArrayList;
import java.util.concurrent.Executors;

public class LanguageListActivity extends BaseActivity {
    private LanguageListAdapter adapter;
    private LanguageListViewModel viewModel;
    private ActivityLanguageListBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLanguageListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupViewModel();
        setupRecyclerView();
        setupSearch();
        setupObservers();

        binding.btnFilter.setOnClickListener(v -> showFilterDialog());
        
        if (binding.fabCreateLanguage != null) {
            binding.fabCreateLanguage.setOnClickListener(v -> openCreateLanguage());
        }

        // Initialize Calculator Drawer Width
        setDrawerWidth(false);
    }

    private void setupViewModel() {
        LanguageRepository repo = new LanguageRepository(
                Open5eDatabase.getInstance(this).documentDao(),
                Open5eDatabase.getInstance(this).languageDao(),
                UserContentDatabase.getInstance(this).customLanguageDao(),
                Executors.newSingleThreadExecutor()
        );

        viewModel = new ViewModelProvider(this, new ViewModelFactory(repo))
                .get(LanguageListViewModel.class);
    }

    private void setupSearch() {
        binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
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

    private void setupRecyclerView() {
        binding.rvLanguages.setLayoutManager(new LinearLayoutManager(this));
        adapter = new LanguageListAdapter(
                new ArrayList<>(),
                this::openDetails,
                this::openCreateLanguage
        );
        binding.rvLanguages.setAdapter(adapter);
    }

    private void setupObservers() {
        viewModel.getFilteredLanguages().observe(this, list -> {
            adapter.updateList(list);
        });
    }

    private void showFilterDialog() {
        DialogLanguageFilterBinding filterBinding = DialogLanguageFilterBinding.inflate(getLayoutInflater());

        String[] types = {"All", "Official", "Custom"};
        filterBinding.spinnerType.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, types));

        new AlertDialog.Builder(this)
                .setTitle("Filter Languages")
                .setView(filterBinding.getRoot())
                .setPositiveButton("Apply", (d, w) -> {
                    viewModel.setTypeFilter(types[filterBinding.spinnerType.getSelectedItemPosition()]);
                    // Here you could add exotic/secret filtering logic to ViewModel if needed
                    binding.tvActiveFilters.setVisibility(View.VISIBLE);
                    binding.tvActiveFilters.setText("Filters applied");
                })
                .setNeutralButton("Clear", (d, w) -> {
                    viewModel.setTypeFilter("All");
                    binding.tvActiveFilters.setVisibility(View.GONE);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void openDetails(CombinedLanguage lang) {
        Intent i = new Intent(this, LanguageDetailActivity.class);
        i.putExtra(LanguageDetailActivity.EXTRA_LANGUAGE, lang);
        startActivity(i);
    }

    private void openCreateLanguage() {
        startActivity(new Intent(this, CustomLanguageCreateActivity.class));
    }
}
