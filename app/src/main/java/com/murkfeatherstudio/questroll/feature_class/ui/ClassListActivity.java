package com.murkfeatherstudio.questroll.feature_class.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.murkfeatherstudio.questroll.R;
import com.murkfeatherstudio.questroll.core.base.BaseActivity;
import com.murkfeatherstudio.questroll.core.local_database.Open5eDatabase;
import com.murkfeatherstudio.questroll.core.local_database.UserContentDatabase;
import com.murkfeatherstudio.questroll.feature_class.class_adapter.ClassAdapter;
import com.murkfeatherstudio.questroll.feature_class.model.CombinedClass;
import com.murkfeatherstudio.questroll.feature_class.repository.ClassRepository;
import com.murkfeatherstudio.questroll.feature_class.ui.wizard.ClassWizardActivity;
import com.murkfeatherstudio.questroll.feature_class.view_model.ClassListViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.concurrent.Executors;

public class ClassListActivity extends BaseActivity {
    private ClassAdapter adapter;
    private ClassListViewModel viewModel;
    private AlertDialog createClassDialog;
    private TextView tvActiveFilters;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_class_list);

        setupViewModel();
        initViews();
        setupRecyclerView();
        setupSearch();
        observeData();

        findViewById(R.id.btn_filter).setOnClickListener(v -> showFilterDialog());
        
        FloatingActionButton fabCreate = findViewById(R.id.fab_create_class);
        if (fabCreate != null) {
            fabCreate.setOnClickListener(v -> showCreateClassDialog());
        }
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
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ClassAdapter(new ClassAdapter.OnItemClickListener() {
            @Override
            public void onClassClick(CombinedClass classEntity) {
                Intent intent = new Intent(ClassListActivity.this, ClassDetailActivity.class);
                intent.putExtra("CLASS_KEY", classEntity.id);
                startActivity(intent);
            }

            @Override
            public void onCreateClick() {
                showCreateClassDialog();
            }
        });
        recyclerView.setAdapter(adapter);
    }

    private void setupViewModel() {
        Open5eDatabase open5eDb = Open5eDatabase.getInstance(this);
        UserContentDatabase customDb = UserContentDatabase.getInstance(this);

        ClassRepository repository = new ClassRepository(
                open5eDb.characterClassDao(),
                customDb.customCharacterClassDao(),
                Executors.newSingleThreadExecutor()
        );

        ClassListViewModel.Factory factory = new ClassListViewModel.Factory(repository);
        viewModel = new ViewModelProvider(this, factory).get(ClassListViewModel.class);
    }

    private void observeData() {
        viewModel.getFilteredClasses().observe(this, classes -> {
            if (classes != null) {
                adapter.submitList(classes);
            }
        });
    }

    private void showFilterDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_class_filter, null);
        Spinner spinnerSystem = dialogView.findViewById(R.id.spinner_system);
        Spinner spinnerType = dialogView.findViewById(R.id.spinner_type);

        String[] systems = {"All", "5e-2014", "5e-2024", "A5E"};
        String[] types = {"All", "Official", "Custom"};

        spinnerSystem.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, systems));
        spinnerType.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, types));

        new AlertDialog.Builder(this)
                .setTitle("Filter Classes")
                .setView(dialogView)
                .setPositiveButton("Apply", (d, w) -> {
                    viewModel.setSystemFilter(systems[spinnerSystem.getSelectedItemPosition()]);
                    viewModel.setTypeFilter(types[spinnerType.getSelectedItemPosition()]);
                    updateFilterSummary();
                })
                .setNeutralButton("Clear", (d, w) -> {
                    viewModel.setSystemFilter("All");
                    viewModel.setTypeFilter("All");
                    tvActiveFilters.setVisibility(View.GONE);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void updateFilterSummary() {
        // Logic to show text summary of active filters
        tvActiveFilters.setVisibility(View.VISIBLE);
        tvActiveFilters.setText("Filters active");
    }

    private void showCreateClassDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_create_class_choice, null);
        builder.setView(dialogView);
        createClassDialog = builder.create();
        createClassDialog.show();

        dialogView.findViewById(R.id.btn_new_class).setOnClickListener(v -> {
            createClassDialog.dismiss();
            openWizard(false);
        });
        dialogView.findViewById(R.id.btn_new_subclass).setOnClickListener(v -> {
            createClassDialog.dismiss();
            openWizard(true);
        });
        dialogView.findViewById(R.id.btn_cancel).setOnClickListener(v -> createClassDialog.dismiss());
    }

    private void openWizard(boolean isSubclass) {
        startActivity(new Intent(this, ClassWizardActivity.class).putExtra("is_subclass", isSubclass));
    }
}
