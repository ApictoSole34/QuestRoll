package com.murkfeatherstudio.questroll.feature_class.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;

import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.murkfeatherstudio.questroll.R;
import com.murkfeatherstudio.questroll.core.base.BaseActivity;
import com.murkfeatherstudio.questroll.core.local_database.Open5eDatabase;
import com.murkfeatherstudio.questroll.core.local_database.UserContentDatabase;
import com.murkfeatherstudio.questroll.databinding.ActivityClassListBinding;
import com.murkfeatherstudio.questroll.databinding.DialogClassFilterBinding;
import com.murkfeatherstudio.questroll.databinding.DialogCreateClassChoiceBinding;
import com.murkfeatherstudio.questroll.feature_class.class_adapter.ClassAdapter;
import com.murkfeatherstudio.questroll.feature_class.model.CombinedClass;
import com.murkfeatherstudio.questroll.feature_class.repository.ClassRepository;
import com.murkfeatherstudio.questroll.feature_class.ui.wizard.ClassWizardActivity;
import com.murkfeatherstudio.questroll.feature_class.view_model.ClassListViewModel;

import java.util.concurrent.Executors;

public class ClassListActivity extends BaseActivity {
    private ClassAdapter adapter;
    private ClassListViewModel viewModel;
    private ActivityClassListBinding binding;
    private AlertDialog createClassDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityClassListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setTitle("Classes");

        setupViewModel();
        setupRecyclerView();
        setupSearch();
        observeData();

        binding.btnFilter.setOnClickListener(v -> showFilterDialog());
        
        if (binding.fabCreateClass != null) {
            binding.fabCreateClass.setOnClickListener(v -> showCreateClassDialog());
        }
    }

    private void setupSearch() {
        binding.searchView.setOnQueryTextListener(new android.widget.SearchView.OnQueryTextListener() {
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
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
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
        binding.recyclerView.setAdapter(adapter);
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
        DialogClassFilterBinding filterBinding = DialogClassFilterBinding.inflate(getLayoutInflater());

        String[] systems = {"All", "5e-2014", "5e-2024", "A5E"};
        String[] types = {"All", "Official", "Custom"};

        filterBinding.spinnerSystem.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, systems));
        filterBinding.spinnerType.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, types));

        new AlertDialog.Builder(this)
                .setTitle("Filter Classes")
                .setView(filterBinding.getRoot())
                .setPositiveButton("Apply", (d, w) -> {
                    viewModel.setSystemFilter(systems[filterBinding.spinnerSystem.getSelectedItemPosition()]);
                    viewModel.setTypeFilter(types[filterBinding.spinnerType.getSelectedItemPosition()]);
                    updateFilterSummary();
                })
                .setNeutralButton("Clear", (d, w) -> {
                    viewModel.setSystemFilter("All");
                    viewModel.setTypeFilter("All");
                    binding.tvActiveFilters.setVisibility(View.GONE);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void updateFilterSummary() {
        binding.tvActiveFilters.setVisibility(View.VISIBLE);
        binding.tvActiveFilters.setText("Filters active");
    }

    private void showCreateClassDialog() {
        DialogCreateClassChoiceBinding choiceBinding = DialogCreateClassChoiceBinding.inflate(getLayoutInflater());
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(choiceBinding.getRoot());
        createClassDialog = builder.create();
        createClassDialog.show();

        choiceBinding.btnNewClass.setOnClickListener(v -> {
            createClassDialog.dismiss();
            openWizard(false);
        });
        choiceBinding.btnNewSubclass.setOnClickListener(v -> {
            createClassDialog.dismiss();
            openWizard(true);
        });
        choiceBinding.btnCancel.setOnClickListener(v -> createClassDialog.dismiss());
    }

    private void openWizard(boolean isSubclass) {
        startActivity(new Intent(this, ClassWizardActivity.class).putExtra("is_subclass", isSubclass));
    }
}
