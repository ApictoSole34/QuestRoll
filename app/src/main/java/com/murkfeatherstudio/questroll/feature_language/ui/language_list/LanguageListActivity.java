package com.murkfeatherstudio.questroll.feature_language.ui.language_list;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.murkfeatherstudio.questroll.R;
import com.murkfeatherstudio.questroll.core.base.BaseActivity;
import com.murkfeatherstudio.questroll.core.local_database.Open5eDatabase;
import com.murkfeatherstudio.questroll.core.local_database.UserContentDatabase;
import com.murkfeatherstudio.questroll.feature_language.data.repository.LanguageRepository;
import com.murkfeatherstudio.questroll.feature_language.ui.language_create.CustomLanguageCreateActivity;
import com.murkfeatherstudio.questroll.feature_language.ui.language_details.LanguageDetailActivity;
import com.murkfeatherstudio.questroll.feature_language.ui.language_list.adapter.LanguageListAdapter;
import com.murkfeatherstudio.questroll.feature_language.model.CombinedLanguage;
import com.murkfeatherstudio.questroll.feature_language.view_model.LanguageListViewModel;
import com.murkfeatherstudio.questroll.feature_language.view_model.ViewModelFactory;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.concurrent.Executors;

public class LanguageListActivity extends BaseActivity {
    private LanguageListAdapter adapter;
    private LanguageListViewModel viewModel;
    private TextView tvActiveFilters;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_language_list);

        setupViewModel();
        initViews();
        setupRecyclerView();
        setupSearch();
        setupObservers();

        findViewById(R.id.btn_filter).setOnClickListener(v -> showFilterDialog());
        
        FloatingActionButton fab = findViewById(R.id.fab_create_language);
        if (fab != null) {
            fab.setOnClickListener(v -> openCreateLanguage());
        }
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

    private void initViews() {
        tvActiveFilters = findViewById(R.id.tv_active_filters);
    }

    private void setupSearch() {
        SearchView searchView = findViewById(R.id.search_view);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
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
        RecyclerView rv = findViewById(R.id.rv_languages);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new LanguageListAdapter(
                new ArrayList<>(),
                this::openDetails,
                this::openCreateLanguage
        );
        rv.setAdapter(adapter);
    }

    private void setupObservers() {
        viewModel.getFilteredLanguages().observe(this, list -> {
            adapter.updateList(list);
        });
    }

    private void showFilterDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_language_filter, null);
        Spinner spinnerType = dialogView.findViewById(R.id.spinner_type);
        CheckBox cbExotic = dialogView.findViewById(R.id.cb_exotic);
        CheckBox cbSecret = dialogView.findViewById(R.id.cb_secret);

        String[] types = {"All", "Official", "Custom"};
        spinnerType.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, types));

        new AlertDialog.Builder(this)
                .setTitle("Filter Languages")
                .setView(dialogView)
                .setPositiveButton("Apply", (d, w) -> {
                    viewModel.setTypeFilter(types[spinnerType.getSelectedItemPosition()]);
                    // Here you could add exotic/secret filtering logic to ViewModel if needed
                    tvActiveFilters.setVisibility(View.VISIBLE);
                    tvActiveFilters.setText("Filters applied");
                })
                .setNeutralButton("Clear", (d, w) -> {
                    viewModel.setTypeFilter("All");
                    tvActiveFilters.setVisibility(View.GONE);
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
